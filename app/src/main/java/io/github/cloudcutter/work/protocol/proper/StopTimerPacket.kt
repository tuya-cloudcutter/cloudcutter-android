/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-9.
 */

package io.github.cloudcutter.work.protocol.proper

import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.ext.buildByteArray
import java.net.InetAddress

class StopTimerPacket(
	profile: ProfileLightleak.Data,
	requestId: Int,
	val timerPeriods: List<Int>,
) : ProperPacket(profile, requestId) {

	override val action = 0x0E
	override val properData: ByteArray
		get() = buildByteArray(
			size = 4 * (timerPeriods.size + 1),
			0xFF00 or timerPeriods.size,
			*timerPeriods.toTypedArray(),
		)
}
