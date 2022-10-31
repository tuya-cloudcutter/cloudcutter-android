/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231t

import io.github.cloudcutter.data.model.ProfileLightleakDataT
import io.github.cloudcutter.work.protocol.buildByteArray
import io.github.cloudcutter.work.protocol.getFinishToken

data class CallbackPacket(
	private val profile: ProfileLightleakDataT,
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
