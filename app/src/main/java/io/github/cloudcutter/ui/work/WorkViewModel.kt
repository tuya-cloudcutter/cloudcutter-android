/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.ui.work

import android.util.Log
import com.hadilq.liveevent.LiveEvent
import com.spectrum.android.ping.Ping
import com.spectrum.android.ping.Ping.PingListener
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cloudcutter.R
import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.data.repository.ProfileRepository
import io.github.cloudcutter.ui.base.BaseViewModel
import io.github.cloudcutter.util.MessageType
import io.github.cloudcutter.util.Text
import io.github.cloudcutter.work.ActionGraph
import io.github.cloudcutter.work.ActionState
import io.github.cloudcutter.work.WorkData
import io.github.cloudcutter.work.action.*
import io.github.cloudcutter.work.event.*
import io.github.cloudcutter.work.protocol.DGRAM_SIZE
import io.github.cloudcutter.work.protocol.proper.ProperPacket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.net.InetAddress
import javax.inject.Inject

@HiltViewModel
class WorkViewModel @Inject constructor(
	private val api: ApiService,
	private val profileRepository: ProfileRepository,
) : BaseViewModel() {
	companion object {
		private const val TAG = "WorkViewModel"
	}

	private lateinit var work: WorkData
	private lateinit var graph: ActionGraph

	val stateList = mutableListOf<ActionState>()
	val stateAddedIndex = Channel<Int>()
	val stateChangedIndex = Channel<Int>()

	val event = LiveEvent<Event>()

	private var messageRemove: Boolean? = null
	private var pingJob: Deferred<Unit>? = null

	private suspend fun Action.start(): ActionState {
		val state = ActionState(this)
		if (this.title != null) {
			Log.d(TAG, "State start: $this")
			stateList += state
			stateAddedIndex.send(stateList.size - 1)
		}
		return state
	}

	private suspend fun ActionState.end() {
		progress = false
		if (this in stateList) {
			Log.d(TAG, "State end: $action")
			stateChangedIndex.send(stateList.indexOf(this))
		}
	}

	private suspend fun ActionState.error(e: Throwable) {
		Log.d(TAG, "State error: $action $e")
		event.postValue(MessageEvent(MessageType.ERROR, action.getErrorText(e)))
		Log.d(TAG, "State sent")
		error = e
		end()
	}

	suspend fun prepare(profileSlug: String): Boolean {
		val state = DummyAction(Text(R.string.action_prepare)).start()
		try {
			val profile = profileRepository.getProfile(profileSlug)
			Log.d(TAG, "Profile: $profile")
			work = WorkData(profile)
			graph = ActionGraph(work)
			Log.d(TAG, "Preparing action graph")
			graph.prepare(api)
			Log.d(TAG, "Building action graph")
			graph.build()
			Log.d(TAG, "Action graph OK")
			state.end()
			return true
		} catch (e: Exception) {
			state.error(e)
			return false
		}
	}

	suspend fun run() {
		var action: Action? = graph.getStartAction()
		while (action != null) {
			val state = action.start()
			val timeout = action.timeout ?: work.actionTimeout
			try {
				Log.d(TAG, "Action run: $action")
				action = withContext(Dispatchers.IO) {
					withTimeout(timeout) {
						runAction(state)
					}
				}
				Log.d(TAG, "Action OK")
			} catch (e: Exception) {
				state.error(e)
				return
			}
			state.end()
		}
		pingJob?.cancel()
		DummyAction(Text(R.string.action_finish)).start().end()
	}

	private suspend fun runAction(state: ActionState): Action? {
		val action = state.action

		if (messageRemove != null) {
			messageRemove = if (messageRemove == true) {
				event.postValue(MessageRemoveEvent())
				null
			} else true
		}

		when (action) {
			is MessageAction -> {
				event.postValue(MessageEvent(action.type, action.text))
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
			event.postValue(LocalIpRequest())
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
				event.postValue(PingFoundEvent(timeMs))
			}

			override fun onPingException(e: java.lang.Exception?, count: Int) {
				event.postValue(PingLostEvent())
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
			event.postValue(WiFiScanRequest())
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
			event.postValue(WiFiScanRequest())
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
			event.postValue(WiFiConnectRequest(network.ssid, action.password))
			event.awaitTimeout<WiFiConnectResponse>(timeout = 20_000)
			DummyAction(Text(R.string.action_connected_to_ssid, network.ssid)).start().end()
			break
		}
	}
}
