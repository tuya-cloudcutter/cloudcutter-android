/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.protocol.stager

import io.github.cloudcutter.data.model.ProfileDataLightleak
import io.github.cloudcutter.protocol.CMD_RUN_PTR
import io.github.cloudcutter.protocol.buildByteArray

abstract class CallPtrPacket(
	profile: ProfileDataLightleak,
) : StagerPacket(profile) {

	abstract val arg1: Int
	abstract val arg2: Int
	abstract val data: ByteArray

	override fun getCommand(): ByteArray {
		return buildByteArray(
			size = 12 + data.size,
			CMD_RUN_PTR,
			arg1,
			arg2,
			data,
		)
	}
}
