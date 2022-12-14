/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.proper

import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.ext.buildByteArray
import java.net.InetAddress

class FlashReadPacket(
	profile: ProfileLightleak.Data,
	requestId: Int,
	val offset: Int,
	val length: Int,
	val maxLength: Int = 1024,
) : ProperPacket(profile, requestId) {

	override val action = 0x01
	override val properData: ByteArray
		get() = buildByteArray(
			size = 12,
			offset,
			length,
			maxLength,
		)
}
