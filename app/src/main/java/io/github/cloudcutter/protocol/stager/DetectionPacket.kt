/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.protocol.stager

import io.github.cloudcutter.data.FlashBasedProfile
import io.github.cloudcutter.protocol.CMD_FINISH
import io.github.cloudcutter.protocol.buildByteArray

data class DetectionPacket(
	val profile: FlashBasedProfile,
	val gadget: FlashBasedProfile.Gadget,
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
