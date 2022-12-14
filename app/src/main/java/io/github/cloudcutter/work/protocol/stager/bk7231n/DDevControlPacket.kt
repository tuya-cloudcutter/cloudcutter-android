/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-31.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231n

import io.github.cloudcutter.data.model.ProfileLightleakDataN
import io.github.cloudcutter.ext.buildByteArray

data class DDevControlPacket(
	private val profile: ProfileLightleakDataN,
	private val command: DDevCommand,
	private val param: ByteArray,
) : StagerPacket(profile) {

	override val target = profile.getGadget("ddev_control")

	override fun getCommand() = buildByteArray(
		size = 4 + param.size,
		/* 0x48 (u32) cmd= */
		command.base + command.value,
		/* 0x4C (void*) param= */
		param,
	)
}
