/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231t

import io.github.cloudcutter.data.model.ProfileLightleakDataT
import io.github.cloudcutter.work.protocol.base.BasePacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class StagerPacket(
	private val profile: ProfileLightleakDataT,
) : BasePacket() {
	companion object {
		const val CMD_FINISH = 0x02
		const val CMD_RUN_INT = 0x00
		const val CMD_RUN_PTR = 0x01
	}

	var fd = 3
	open val storeAddress = profile.addressMap.store

	override fun getJsonFields() = mapOf(
		"ssid" to "1".toByteArray(),
		"passwd" to "1".toByteArray(),
		"token" to "1".toByteArray(),
	)

	override fun getOptions(): ByteArray? {
		val buf = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
		buf.putInt(fd)
		buf.putInt(profile.addressMap.magic)
		buf.putInt(storeAddress)
		return buf.array()
	}
}
