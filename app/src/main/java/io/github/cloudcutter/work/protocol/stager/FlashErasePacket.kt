/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.work.protocol.stager

import io.github.cloudcutter.data.model.ProfileDataLightleak

data class FlashErasePacket(
	val profile: ProfileDataLightleak,
	val offset: Int,
) : CallIntPacket(profile) {

	override val storeAddress = profile.getGadget("flash_erase_sector").getStoreOffset()
	override val arg1 = offset
}
