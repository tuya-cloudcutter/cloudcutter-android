/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-31.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231n

import io.github.cloudcutter.data.model.ProfileLightleakDataN
import io.github.cloudcutter.work.protocol.getFinishToken

data class CallbackPacket(
	private val profile: ProfileLightleakDataN,
) : StagerPacket(profile) {

	override fun getJsonFields() = mapOf(
		"ssid" to "1".toByteArray(),
		// make the payload bigger to align "tail" to 4 bytes
		"passwd" to "12".toByteArray(),
		"token" to getFinishToken(profile.addressMap.stager),
	)

	// put empty command data right after the actual packet
	override fun getCommand() = byteArrayOf()
	override fun getCommandOffset() = 0x88

	// use the "finish" gadget to call the stager without doing anything
	override val target = profile.getGadget("finish")
}
