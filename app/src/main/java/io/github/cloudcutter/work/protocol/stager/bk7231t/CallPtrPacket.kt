/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231t

import io.github.cloudcutter.data.model.ProfileLightleakDataT
import io.github.cloudcutter.work.protocol.buildByteArray

abstract class CallPtrPacket(
	profile: ProfileLightleakDataT,
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
