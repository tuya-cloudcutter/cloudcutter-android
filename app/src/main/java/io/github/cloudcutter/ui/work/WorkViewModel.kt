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
import io.github.cloudcutter.data.model.Profile
import io.github.cloudcutter.data.repository.ProfileRepository
import io.github.cloudcutter.ext.toHexString
import io.github.cloudcutter.ui.base.BaseViewModel
import io.github.cloudcutter.util.MessageType
import io.github.cloudcutter.util.Text
import io.github.cloudcutter.work.ActionGraph
import io.github.cloudcutter.work.ActionState
import io.github.cloudcutter.work.WorkData
import io.github.cloudcutter.work.action.*
import io.github.cloudcutter.work.event.*
import io.github.cloudcutter.work.protocol.proper.ProperPacket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
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

	private suspend fun Action.start(index: Int? = null): ActionState {
		val state = ActionState(this)
		if (this.title != null) {
			Log.d(TAG, "State start: $this")
			val position = when {
				index == null -> stateList.size
				index < 0 -> (stateList.size + index).coerceAtLeast(0)
				else -> index
			}
			stateList.add(position, state)
			stateAddedIndex.send(position)
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
		error = (e as? CancellationException)?.cause ?: e
		Log.d(TAG, "State error: $action $error")
		event.postValue(MessageEvent(MessageType.ERROR, action.getErrorText(error!!)))
		Log.d(TAG, "State sent")
		end()
	}

	suspend fun prepare(profileSlug: String): Profile<*>? {
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
			return profile
		} catch (e: Exception) {
			state.error(e)
			return null
		}
	}

	suspend fun run(startActionId: String? = null) {
		var action: Action? = startActionId?.let {
			graph.getAction(it)
		} ?: graph.getStartAction()

		while (action != null) {
			val state = action.start()
			val timeout = action.timeout ?: work.actionTimeout
			try {
				Log.d(TAG, "Action run: $action")
				action = withContext(Dispatchers.IO) {
					withTimeout(timeout) {
						delay(100)
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
				Log.d(TAG, "Removing message")
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
			is PingAction -> runPingAction(action)
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
			val packet = action.packet.serialize()
			send.writeFully(packet)
			Log.d(TAG, "Wrote packet: ${packet.toHexString()}")
			socket.close()
			selectorManager.close()
		}
	}

	private suspend fun runPingAction(action: PingAction) = withContext(Dispatchers.IO) {
		var count = 0
		val ping = Ping(InetAddress.getByName(action.address), object : PingListener {
			override fun onPing(timeMs: Long, index: Int) {
				if (timeMs == -1L) {
					onPingException(null, index)
					return
				}
				event.postValue(PingFoundEvent(timeMs))
				if (action.mode == PingAction.Mode.FOUND) count++
			}

			override fun onPingException(e: Exception?, index: Int) {
				event.postValue(PingLostEvent())
				if (action.mode == PingAction.Mode.LOST) count++
			}
		})
		ping.delayMs = 0 // disable blocking delay
		ping.timeoutMs = 1000
		ping.count = 1

		while (count < action.threshold) {
			ping.run()
			delay(1000)
		}
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

		Log.d(TAG, "CustomAP connected")
		withContext(Dispatchers.IO) {
			val error = try {
				val packet = action.buildPacket()
				send.writeFully(packet)
				Log.d(TAG, "Wrote packet: ${packet.toHexString()}")
				val code = recv.readByte().toUByte().toInt()
				Log.d(TAG, "Got response: $code")
				if (code != 0xDE) RuntimeException("Error configuring CustomAP: 0x${code.toString(16)}")
				else null
			} catch (e: TimeoutCancellationException) {
				e
			} finally {
				socket.close()
				selectorManager.close()
			}
			if (error != null) throw error
		}
	}

	private suspend fun runWiFiConnectAction(action: WiFiConnectAction) {
		while (true) {
			event.postValue(WiFiScanRequest())
			val scanResponse = event.await<WiFiScanResponse>()
			val network = scanResponse.networks.firstOrNull {
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
			DummyAction(Text(R.string.action_scanned_ssid, network.ssid)).start(index = -1).end()
			event.postValue(WiFiConnectRequest(network.ssid, action.password))
			event.awaitTimeout<WiFiConnectResponse>(timeout = 20_000)
			DummyAction(Text(R.string.action_connected_to_ssid, network.ssid)).start().end()
			break
		}
	}
}
