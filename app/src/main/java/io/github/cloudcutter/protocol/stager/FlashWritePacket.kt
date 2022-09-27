/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.protocol.stager

import io.github.cloudcutter.data.FlashBasedProfile

data class FlashWritePacket(
	val profile: FlashBasedProfile,
	val offset: Int,
	override val data: ByteArray,
) : CallPtrPacket(profile) {

	override val storeAddress = profile.getGadget("flash_write").getStoreOffset(profile)
	override val arg1 = offset
	override val arg2 = data.size
}
