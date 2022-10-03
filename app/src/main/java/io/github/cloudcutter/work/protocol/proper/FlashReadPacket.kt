/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.proper

import io.github.cloudcutter.data.model.ProfileDataLightleak
import io.github.cloudcutter.work.protocol.buildByteArray

data class FlashReadPacket(
	val profile: ProfileDataLightleak,
	val offset: Int,
	val length: Int,
	val maxLength: Int = 1024,
) : ProperPacket(profile) {

	override val action = 0x01
	override val data: ByteArray
		get() = buildByteArray(
			size = 20,
			profile.addressMap.buffer,
			offset,
			length,
			maxLength,
			returnIp.address.reversedArray(),
		)
}
