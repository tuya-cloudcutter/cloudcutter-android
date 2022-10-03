/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.stager

import io.github.cloudcutter.data.model.ProfileDataLightleak
import io.github.cloudcutter.work.protocol.CMD_RUN_INT
import io.github.cloudcutter.work.protocol.buildByteArray

abstract class CallIntPacket(
	profile: ProfileDataLightleak,
) : StagerPacket(profile) {

	abstract val arg1: Int

	override fun getCommand() = buildByteArray(
		size = 8,
		CMD_RUN_INT,
		arg1,
	)
}
