/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-29.
 */

package io.github.cloudcutter.work

import io.github.cloudcutter.R
import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.data.model.ProfileLightleakDataT
import io.github.cloudcutter.ext.roundTo
import io.github.cloudcutter.util.MessageType
import io.github.cloudcutter.util.Text
import io.github.cloudcutter.work.action.Action
import io.github.cloudcutter.work.action.MessageAction
import io.github.cloudcutter.work.action.PacketAction
import io.github.cloudcutter.work.action.PingAction
import io.github.cloudcutter.work.protocol.proper.StopTimerPacket
import io.github.cloudcutter.work.protocol.stager.bk7231t.CallbackPacket
import io.github.cloudcutter.work.protocol.stager.bk7231t.DetectionPacket
import io.github.cloudcutter.work.protocol.stager.bk7231t.FlashErasePacket
import io.github.cloudcutter.work.protocol.stager.bk7231t.FlashWritePacket
import io.github.cloudcutter.work.protocol.toOffset

class ActionGraphLightleakT(
	private val work: WorkData,
	private val profile: ProfileLightleakDataT,
) {

	fun getActions(nextId: String): Array<Action> = listOf(
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
			autoClear = false,
			nextId = "detect_0",
		),
		*getDetectionActions(nextId = "ping_found_4"),
		PingAction(
			id = "ping_found_4",
			title = Text(R.string.action_ping_respond),
			nextId = "message_exploited",
			mode = PingAction.Mode.FOUND,
			address = work.targetAddress,
		),
		MessageAction(
			id = "message_exploited",
			type = MessageType.SUCCESS,
			text = Text(R.string.message_exploited),
			autoClear = false,
			nextId = "flash_erase",
		),
		*getProperWriteActions(nextId = "proper_stop_timer"),
		PacketAction(
			id = "proper_stop_timer",
			title = Text(R.string.action_packet_stop_timer),
			nextId = nextId,
			packet = StopTimerPacket(
				profile = profile,
				requestId = 0x1234,
				timerPeriods = listOf(3 * 60 * 1000), // 180000 ms
			),
		),
	).toTypedArray()

	private fun getDetectionActions(nextId: String?): Array<Action> {
		val list = mutableListOf<Action>()
		val gadgets = profile.gadgets.filter { it.intfOffset != null || it.name == "proper" }
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

	private fun getProperWriteActions(nextId: String?): Array<Action> {
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
