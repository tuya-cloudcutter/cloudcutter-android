/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231t

import io.github.cloudcutter.data.model.ProfileLightleakDataT

data class FlashErasePacket(
	private val profile: ProfileLightleakDataT,
	private val offset: Int,
) : CallIntPacket(profile) {

	override val storeAddress = profile.getGadget("flash_erase_sector").getStoreOffset(profile)
	override val arg1 = offset
}
