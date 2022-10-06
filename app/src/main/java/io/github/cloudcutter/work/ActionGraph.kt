/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work

import android.util.Log
import io.github.cloudcutter.R
import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.data.api.checkResponse
import io.github.cloudcutter.data.model.ProfileClassic
import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.ext.roundTo
import io.github.cloudcutter.ext.toHexString
import io.github.cloudcutter.util.MessageType
import io.github.cloudcutter.util.Text
import io.github.cloudcutter.work.action.*
import io.github.cloudcutter.work.protocol.CloudcutPacket
import io.github.cloudcutter.work.protocol.WifiPacket
import io.github.cloudcutter.work.protocol.stager.CallbackPacket
import io.github.cloudcutter.work.protocol.stager.DetectionPacket
import io.github.cloudcutter.work.protocol.stager.FlashErasePacket
import io.github.cloudcutter.work.protocol.stager.FlashWritePacket
import io.github.cloudcutter.work.protocol.toOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("SameParameterValue")
class ActionGraph(private val work: WorkData) {
	companion object {
		private const val TAG = "ActionGraph"
	}

	private val randomHex: List<Char> = ('a'..'f') + ('0'..'9')
	private val randomAscii: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

	private lateinit var actions: List<Action>

	suspend fun prepare(api: ApiService) = withContext(Dispatchers.IO) {
		when (work.profile) {
			is ProfileClassic -> {
				work.newUuid = (0 until 16).map { randomHex.random() }.joinToString("")
				work.newAuthKey = (0 until 32).map { randomAscii.random() }.joinToString("")
			}
			is ProfileLightleak -> {
				work.lightleakPassword = downloadBinary(api, work.profile.data.bins.stager)
				work.lightleakProper = downloadBinary(api, work.profile.data.bins.proper)
			}
		}
	}

	private suspend fun downloadBinary(api: ApiService, name: String): ByteArray {
		Log.d(TAG, "Downloading '${name}'")
		val response = api.getBinary(name).checkResponse()
		return response.bytes()
	}

	fun build() {
		actions = when (work.profile) {
			is ProfileClassic -> buildClassic(work.profile.data)
			is ProfileLightleak -> buildLightleak(work.profile.data)
			else -> throw IllegalArgumentException("Unknown profile type")
		}

		for (action in actions.filterIsInstance<PacketAction>()) {
			Log.d(TAG, "$action packet: ${action.packet.serialize().toHexString()}")
		}
	}

	fun getStartAction() = actions.first()
	fun getAction(id: String) = actions.firstOrNull { it.id == id }
		?: throw NoSuchElementException("No action with id=$id")

	private fun buildClassic(profile: ProfileClassic.Data) = listOf(
		MessageAction(
			id = "message_device_connect_1",
			type = MessageType.INFO,
			text = Text(R.string.message_enable_ap_pairing),
			nextId = "connect_to_device",
		),
		WiFiConnectAction(
			id = "connect_to_device",
			title = Text(R.string.action_connect_to_device),
			nextId = "ping_found_1",
			type = WiFiConnectAction.Type.DEVICE_DEFAULT,
			ssid = null,
			password = null,
		),
		PingAction(
			id = "ping_found_1",
			title = Text(R.string.action_ping_connect),
			nextId = "exploit_initial",
			mode = PingAction.Mode.FOUND,
			address = work.targetAddress,
		),
		PacketAction(
			id = "exploit_initial",
			title = Text(R.string.action_packet_cloudcut),
			nextId = "ping_lost_1",
			packet = CloudcutPacket(
				data = profile,
				apSsid = work.targetSsidPrefix.take(work.targetSsidPrefix.length - 1),
				newUuid = work.newUuid,
				newAuthKey = work.newAuthKey,
				newPskKey = "",
			),
		),
		PingAction(
			id = "ping_lost_1",
			title = Text(R.string.action_ping_lost),
			nextId = "connect_custom_ssid",
			mode = PingAction.Mode.LOST,
			address = work.targetAddress,
		),
		WiFiConnectAction(
			id = "connect_custom_ssid",
			title = Text(R.string.action_connect_custom_ssid),
			nextId = "ping_found_2",
			type = WiFiConnectAction.Type.DEVICE_CUSTOM,
			ssid = work.targetSsidPrefix,
		),
		PingAction(
			id = "ping_found_2",
			title = Text(R.string.action_ping_connect),
			nextId = "exploit_initial",
			mode = PingAction.Mode.FOUND,
			address = work.targetAddress,
		),
	)

	private fun buildLightleak(profile: ProfileLightleak.Data) = listOf(
		MessageAction(
			id = "message_custom_ap_connect",
			type = MessageType.INFO,
			text = Text(R.string.message_start_custom_ap, work.idleSsid),
			nextId = "custom_ap_connect",
		),
		WiFiConnectAction(
			id = "custom_ap_connect",
			title = Text(R.string.action_custom_ap_connect, work.idleSsid),
			nextId = "ap_ping_found_1",
			type = WiFiConnectAction.Type.SSID,
			ssid = work.idleSsid,
			password = work.idlePassword,
		),
		PingAction(
			id = "ap_ping_found_1",
			title = Text(R.string.action_ping_connect),
			nextId = "custom_ap_setup",
			mode = PingAction.Mode.FOUND,
			address = work.idleAddress,
		),
		WiFiCustomAPAction(
			id = "custom_ap_setup",
			title = Text(R.string.action_custom_ap_setup),
			nextId = "message_device_connect_1",
			ssid = work.lightleakSsid,
			password = work.lightleakPassword,
			stopTimeout = 8_000,
		),
		MessageAction(
			id = "message_device_connect_1",
			type = MessageType.INFO,
			text = Text(R.string.message_enable_ap_pairing),
			nextId = "connect_default_1",
		),
		WiFiConnectAction(
			id = "connect_default_1",
			title = Text(R.string.action_connect_to_device),
			nextId = "ping_found_1",
			type = WiFiConnectAction.Type.DEVICE_DEFAULT,
			ssid = null,
		),
		PingAction(
			id = "ping_found_1",
			title = Text(R.string.action_ping_connect),
			nextId = "exploit_stager",
			mode = PingAction.Mode.FOUND,
			address = work.targetAddress,
		),
		PacketAction(
			id = "exploit_stager",
			title = Text(R.string.action_packet_stager),
			nextId = "ping_lost_1",
			packet = WifiPacket(
				ssid = work.lightleakSsid,
				password = work.lightleakPassword,
				token = "1".toByteArray(),
			),
		),
		PingAction(
			id = "ping_lost_1",
			title = Text(R.string.action_ping_lost),
			nextId = "custom_ap_scan",
			mode = PingAction.Mode.LOST,
			address = work.targetAddress,
		),
		WiFiScanAction(
			id = "custom_ap_scan",
			title = Text(R.string.action_custom_ap_wait_timeout),
			nextId = "message_device_reboot",
			ssid = work.idleSsid,
			timeout = 30_000,
		),
		MessageAction(
			id = "message_device_reboot",
			type = MessageType.INFO,
			text = Text(R.string.message_device_reboot_ap_mode),
			nextId = "connect_default_2",
		),
		WiFiConnectAction(
			id = "connect_default_2",
			title = Text(R.string.action_connect_to_device),
			nextId = "ping_found_2",
			type = WiFiConnectAction.Type.DEVICE_DEFAULT,
			ssid = null,
		),
		PingAction(
			id = "ping_found_2",
			title = Text(R.string.action_ping_connect),
			timeout = 20_000,
			nextId = "exploit_check",
			mode = PingAction.Mode.FOUND,
			address = work.targetAddress,
		),
		PacketAction(
			id = "exploit_check",
			title = Text(R.string.action_packet_stager_callback),
			nextId = "ping_found_3",
			packet = CallbackPacket(
				profile = profile,
			),
		),
		PingAction(
			id = "ping_found_3",
			title = Text(R.string.action_ping_exploitable),
			nextId = "message_exploitable",
			mode = PingAction.Mode.FOUND,
			address = work.targetAddress,
		),
		MessageAction(
			id = "message_exploitable",
			type = MessageType.SUCCESS,
			text = Text(R.string.message_exploitable),
			nextId = "detect_0",
		),
		*getDetectionActions(profile, nextId = "ping_found_4"),
		PingAction(
			id = "ping_found_4",
			title = Text(R.string.action_ping_respond),
			nextId = "flash_erase",
			mode = PingAction.Mode.FOUND,
			address = work.targetAddress,
		),
		*getProperWriteActions(profile, nextId = ""),
	)

	private fun getDetectionActions(
		profile: ProfileLightleak.Data,
		nextId: String,
	): Array<Action> {
		val list = mutableListOf<Action>()
		val gadgets = profile.gadgets.filter { it.intfOffset != null }
		for ((i, gadget) in gadgets.withIndex()) {
			list += PacketAction(
				id = "detect_${i}",
				title = Text(R.string.action_packet_detect_gadget, gadget.name),
				nextId = if (i == gadgets.lastIndex) nextId else "detect_${i + 1}",
				packet = DetectionPacket(
					profile = profile,
					gadget = gadget,
				),
			)
		}
		return list.toTypedArray()
	}

	private fun getProperWriteActions(
		profile: ProfileLightleak.Data,
		nextId: String,
	): Array<Action> {
		var offset = profile.getGadget("proper").map.values.first().roundTo(2).toOffset()
		val list = mutableListOf<Action>(
			PacketAction(
				id = "flash_erase",
				title = Text(R.string.action_packet_flash_erase, offset),
				nextId = "flash_write_0",
				packet = FlashErasePacket(
					profile = profile,
					offset = offset,
				),
			),
		)

		val chunked = work.lightleakProper.toList().chunked(128)
		for ((i, chunk) in chunked.withIndex()) {
			list += PacketAction(
				id = "flash_write_${i}",
				title = Text(R.string.action_packet_flash_write, chunk.size, offset),
				nextId = if (i == chunked.lastIndex) nextId else "flash_write_${i + 1}",
				packet = FlashWritePacket(
					profile = profile,
					offset = offset,
					data = chunk.toByteArray(),
				),
			)
			offset += chunk.size
		}
		return list.toTypedArray()
	}
}
