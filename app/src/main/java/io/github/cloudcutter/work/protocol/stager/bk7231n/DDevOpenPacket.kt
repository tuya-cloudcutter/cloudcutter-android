/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-31.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231n

import io.github.cloudcutter.data.model.ProfileLightleakDataN
import io.github.cloudcutter.work.protocol.buildByteArray

data class DDevOpenPacket(
	private val profile: ProfileLightleakDataN,
	private val name: String,
) : StagerPacket(profile) {

	override val target = profile.getGadget("ddev_open")

	override fun getCommand() = buildByteArray(
		size = 8 + name.length + 1,
		/* 0x48 (u32*) status= */
		profile.addressMap.temp,
		/* 0x4C (u32) op_flag= */
		0,
		/* 0x50 (char*) dev_name= */
		name,
	)
}
