/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231t

import io.github.cloudcutter.data.model.ProfileLightleakDataT
import io.github.cloudcutter.ext.buildByteArray

abstract class CallIntPacket(
	profile: ProfileLightleakDataT,
) : StagerPacket(profile) {

	abstract val arg1: Int

	override fun getCommand() = buildByteArray(
		size = 8,
		CMD_RUN_INT,
		arg1,
	)
}
