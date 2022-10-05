/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.work.protocol.stager

import io.github.cloudcutter.data.model.ProfileLightleak

data class FlashWritePacket(
	val profile: ProfileLightleak.Data,
	val offset: Int,
	override val data: ByteArray,
) : CallPtrPacket(profile) {

	override val storeAddress = profile.getGadget("flash_write").getStoreOffset(profile)
	override val arg1 = offset
	override val arg2 = data.size
}
