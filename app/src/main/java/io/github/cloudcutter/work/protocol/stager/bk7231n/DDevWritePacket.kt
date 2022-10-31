/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-31.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231n

import io.github.cloudcutter.data.model.ProfileLightleakDataN
import io.github.cloudcutter.work.protocol.buildByteArray

data class DDevWritePacket(
	private val profile: ProfileLightleakDataN,
	private val data: ByteArray,
	private val param: Int,
) : StagerPacket(profile) {

	override val target = profile.getGadget("ddev_write")

	override fun getCommand() = buildByteArray(
		size = 8 + data.size,
		/* 0x48 (u32) count= */
		data.size,
		/* 0x4C (u32) param= */
		param,
		/* 0x50 (u8*) data= */
		data,
	)
}
