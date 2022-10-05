/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.stager

import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.work.protocol.CMD_RUN_PTR
import io.github.cloudcutter.work.protocol.buildByteArray

abstract class CallPtrPacket(
	profile: ProfileLightleak.Data,
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