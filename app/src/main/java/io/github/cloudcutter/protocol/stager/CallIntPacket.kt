/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.protocol.stager

import io.github.cloudcutter.data.FlashBasedProfile
import io.github.cloudcutter.protocol.CMD_RUN_INT
import io.github.cloudcutter.protocol.buildByteArray

abstract class CallIntPacket(
	profile: FlashBasedProfile,
) : StagerPacket(profile) {

	abstract val arg1: Int

	override fun getCommand() = buildByteArray(
		size = 8,
		CMD_RUN_INT,
		arg1,
	)
}
