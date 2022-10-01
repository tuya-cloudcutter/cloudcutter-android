/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.protocol.stager

import io.github.cloudcutter.data.model.ProfileDataLightleak
import io.github.cloudcutter.protocol.CMD_FINISH
import io.github.cloudcutter.protocol.buildByteArray
import io.github.cloudcutter.protocol.getFinishToken

data class CallbackPacket(
	val profile: ProfileDataLightleak,
) : StagerPacket(profile) {

	override fun getJsonFields() = mapOf(
		"ssid" to "1".toByteArray(),
		// make the payload bigger to align "tail" to 4 bytes
		"passwd" to "12".toByteArray(),
		"token" to getFinishToken(profile.addressMap.stager),
	)

	// put the finish command right after the actual packet
	override fun getCommand() = buildByteArray(size = 4, CMD_FINISH)
	override fun getCommandOffset() = 0x88
}
