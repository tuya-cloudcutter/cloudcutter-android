/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231t

import io.github.cloudcutter.data.model.ProfileLightleakDataT
import io.github.cloudcutter.work.protocol.buildByteArray

data class DetectionPacket(
	private val profile: ProfileLightleakDataT,
	private val gadget: ProfileLightleakDataT.Gadget,
) : StagerPacket(profile) {

	override val storeAddress =
		gadget.intfOffset?.plus(profile.addressMap.intf) ?: profile.addressMap.store

	override fun getCommand(): ByteArray {
		val items = gadget.map.flatMap { (k, v) -> listOf(k, v) }
		return buildByteArray(
			size = items.size * 4 + 4,
			*items.toTypedArray(),
			CMD_FINISH,
		)
	}
}
