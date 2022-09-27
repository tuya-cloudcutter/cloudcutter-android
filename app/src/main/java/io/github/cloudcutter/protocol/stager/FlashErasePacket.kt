/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.protocol.stager

import io.github.cloudcutter.data.FlashBasedProfile

data class FlashErasePacket(
	val profile: FlashBasedProfile,
	val offset: Int,
) : CallIntPacket(profile) {

	override val storeAddress = profile.getGadget("flash_erase_sector").getStoreOffset(profile)
	override val arg1 = offset
}
