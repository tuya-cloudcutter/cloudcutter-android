/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.stager

import io.github.cloudcutter.data.model.ProfileDataLightleak
import io.github.cloudcutter.work.protocol.OPT_LENGTH
import io.github.cloudcutter.work.protocol.base.BasePacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

abstract class StagerPacket(
	profile: ProfileDataLightleak,
) : BasePacket() {

	var fd = 3
	val magicAddress = profile.addressMap.magic
	open val storeAddress = profile.addressMap.store

	override fun getJsonFields() = mapOf(
		"ssid" to "1".toByteArray(),
		"passwd" to "1".toByteArray(),
		"token" to "1".toByteArray(),
	)

	override fun getOptions(): ByteArray? {
		val buf = ByteBuffer.allocate(OPT_LENGTH).order(ByteOrder.LITTLE_ENDIAN)
		buf.putInt(fd)
		buf.putInt(magicAddress)
		buf.putInt(storeAddress)
		return buf.array()
	}
}
