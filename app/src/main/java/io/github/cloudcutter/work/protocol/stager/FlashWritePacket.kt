/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.work.protocol.stager

import io.github.cloudcutter.data.model.ProfileDataLightleak

data class FlashWritePacket(
	val profile: ProfileDataLightleak,
	val offset: Int,
	override val data: ByteArray,
) : CallPtrPacket(profile) {

	override val storeAddress = profile.getGadget("flash_write").getStoreOffset()
	override val arg1 = offset
	override val arg2 = data.size
}
