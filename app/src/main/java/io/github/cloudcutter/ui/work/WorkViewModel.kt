/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.ui.work

import androidx.lifecycle.ViewModel
import com.spectrum.android.ping.Ping
import com.spectrum.android.ping.Ping.PingListener
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cloudcutter.R
import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.data.model.Profile
import io.github.cloudcutter.util.Text
import io.github.cloudcutter.work.ActionGraph
import io.github.cloudcutter.work.WorkData
import io.github.cloudcutter.work.action.*
import io.github.cloudcutter.work.event.*
import io.github.cloudcutter.work.protocol.DGRAM_SIZE
import io.github.cloudcutter.work.protocol.proper.ProperPacket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import java.net.InetAddress
import javax.inject.Inject

@HiltViewModel
class WorkViewModel @Inject constructor(
	private val api: ApiService,
) : ViewModel() {

	private lateinit var work: WorkData
	private lateinit var graph: ActionGraph

	val stateList = mutableListOf<ActionState>()
	val stateChanged = MutableSharedFlow<ActionState>()

	val event = Channel<Event>()

	private var messageRemove: Boolean? = null
	private var pingJob: Deferred<Unit>? = null

	inner class ActionState(val action: Action) {
		var progress: Boolean = true
		var error: Throwable? = null

		suspend fun end() {
			progress = false
			stateChanged.emit(this)
		}

		suspend fun error(e: Throwable) {
			error = e
			end()
		}
	}

	private suspend fun stateStart(action: Action): ActionState {
		val state = ActionState(action)
		stateList += state
		stateChanged.emit(state)
		return state
	}

	suspend fun prepare(profile: Profile) {
		val state = stateStart(DummyAction(Text(R.string.action_prepare)))
		try {
			work = WorkData(profile)
			graph = ActionGraph(work)
			graph.prepare(api)
			graph.build()
			state.end()
		} catch (e: Exception) {
			state.error(e)
		}
	}

	suspend fun run() {
		var action: Action? = graph.getStartAction()
		while (action != null) {
			val state = stateStart(action)
			val timeout = action.timeout ?: work.actionTimeout
			try {
				action = withContext(Dispatchers.IO) {
					withTimeout(timeout) {
						runAction(state)
					}
				}
			} catch (e: Exception) {
				state.error(e)
				return
			}
			state.end()
		}
		pingJob?.cancel()
		stateStart(DummyAction(Text(R.string.action_finish))).end()
	}

	private suspend fun runAction(state: ActionState): Action? {
		val action = state.action

		if (messageRemove != null) {
			messageRemove = if (messageRemove == true) {
				event.send(MessageRemoveEvent())
				null
			} else true
		}

		when (action) {
			is MessageAction -> {
				event.send(MessageEvent(action.type, action.text))
				messageRemove = false
			}
			is PacketAction -> runPacketAction(action)
			is PingStartAction -> runPingAction(action)
			is PingWaitFoundAction -> event.await<PingFoundEvent>()
			is PingWaitLostAction -> event.await<PingLostEvent>()
			is WiFiConnectAction -> runWiFiConnectAction(action)
			is WiFiCustomAPAction -> runWiFiCustomAPAction(action)
			is WiFiScanAction -> runWiFiScanAction(action)
		}

		if (action.nextId != null) {
			return graph.getAction(action.nextId)
		}
		return null
	}

	private suspend fun runPacketAction(action: PacketAction) {
		val selectorManager = SelectorManager(Dispatchers.IO)
		val socket = aSocket(selectorManager).udp().connect(
			remoteAddress = InetSocketAddress(work.targetBroadcast, 6669),
			localAddress = null,
			configure = {
				broadcast = true
			},
		)
		val send = socket.openWriteChannel(autoFlush = true)

		if (action.packet is ProperPacket) {
			event.send(LocalIpRequest())
			val localAddress = event.await<LocalIpResponse>().address
			action.packet.returnIp = localAddress
		}

		withContext(Dispatchers.IO) {
			send.writeFully(action.packet.serialize(), 0, DGRAM_SIZE)
			socket.close()
			selectorManager.close()
		}
	}

	private suspend fun runPingAction(action: PingStartAction) = withContext(Dispatchers.IO) {
		val ping = Ping(InetAddress.getByName(action.address), object : PingListener {
			override fun onPing(timeMs: Long, index: Int) {
				event.trySend(PingFoundEvent(timeMs))
			}

			override fun onPingException(e: java.lang.Exception?, count: Int) {
				event.trySend(PingLostEvent())
			}
		})
		pingJob = async(Dispatchers.IO) {
			while (true) {
				ping.run()
			}
		}
		return@withContext action.nextId
	}

	private suspend fun runWiFiScanAction(action: WiFiScanAction) {
		while (true) {
			event.send(WiFiScanRequest())
			val response = event.await<WiFiScanResponse>()
			response.networks.firstOrNull {
				it.ssid == action.ssid
			} ?: continue
			return
		}
	}

	private suspend fun runWiFiCustomAPAction(action: WiFiCustomAPAction) {
		val selectorManager = SelectorManager(Dispatchers.IO)
		val socket = aSocket(selectorManager).tcp().connect(work.idleAddress, work.idlePort)
		val recv = socket.openReadChannel()
		val send = socket.openWriteChannel(autoFlush = true)

		withContext(Dispatchers.IO) {
			send.writeFully(action.buildPacket())
			val code = recv.readByte().toInt()
			socket.close()
			selectorManager.close()
			if (code != 0xDE) {
				throw RuntimeException("Error configuring CustomAP: 0x${code.toString(16)}")
			}
		}
	}

	private suspend fun runWiFiConnectAction(action: WiFiConnectAction) {
		while (true) {
			event.send(WiFiScanRequest())
			val response = event.await<WiFiScanResponse>()
			val network = response.networks.firstOrNull {
				when (action.type) {
					WiFiConnectAction.Type.DEVICE_DEFAULT -> {
						!it.isEncrypted && it.ssid.matches(work.targetSsidRegex)
					}
					WiFiConnectAction.Type.DEVICE_CUSTOM -> {
						!it.isEncrypted && it.ssid.startsWith(action.ssid ?: "")
					}
					WiFiConnectAction.Type.SSID -> {
						it.ssid == action.ssid
					}
				}
			} ?: continue
			event.send(WiFiConnectRequest(network.ssid, action.password))
			event.awaitTimeout<WiFiConnectResponse>(timeout = 5_000)
			stateStart(DummyAction(Text(R.string.action_connected_to_ssid, network.ssid))).end()
			break
		}
	}
}
