/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.proper

import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.work.protocol.stager.CallPtrPacket
import java.net.Inet4Address
import java.net.InetAddress

abstract class ProperPacket(
	profile: ProfileLightleak.Data,
) : CallPtrPacket(profile) {

	var returnIp: InetAddress = Inet4Address.getByName("255.255.255.255")

	abstract val action: Int

	override val storeAddress = profile.getGadget("proper").getStoreOffset(profile)
	override val arg1 = profile.addressMap.intf
	override val arg2
		get() = action
}
