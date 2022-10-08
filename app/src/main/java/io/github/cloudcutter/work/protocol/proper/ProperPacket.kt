/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.proper

import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.work.protocol.buildByteArray
import io.github.cloudcutter.work.protocol.stager.CallPtrPacket
import java.net.Inet4Address
import java.net.InetAddress

abstract class ProperPacket(
	val profile: ProfileLightleak.Data,
	val requestId: Int,
	var returnIp: InetAddress = InetAddress.getByName("255.255.255.255"),
) : CallPtrPacket(profile) {

	abstract val action: Int
	abstract val properData: ByteArray

	override val storeAddress = profile.getGadget("proper").getStoreOffset(profile)
	override val arg1 = profile.addressMap.intf
	override val arg2
		get() = action

	override val data: ByteArray
		get() {
			val command = properData
			return buildByteArray(
				size = 12 + command.size,
				requestId,
				returnIp.address.reversedArray(),
				profile.addressMap.buffer,
				command,
			)
		}
}
