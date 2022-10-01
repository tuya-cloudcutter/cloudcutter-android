/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.protocol

import io.github.cloudcutter.data.model.Profile
import io.github.cloudcutter.data.model.ProfileDataClassic
import io.github.cloudcutter.data.model.ProfileDataLightleak
import io.github.cloudcutter.protocol.base.IPacket
import io.github.cloudcutter.protocol.proper.FlashReadPacket
import io.github.cloudcutter.protocol.proper.ProperPacket
import io.github.cloudcutter.protocol.stager.CallbackPacket
import io.github.cloudcutter.protocol.stager.DetectionPacket
import io.github.cloudcutter.protocol.stager.FlashErasePacket
import io.github.cloudcutter.protocol.stager.FlashWritePacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class LanProtocol(
	val profile: Profile,
) {

	private fun requireClassic(): ProfileDataClassic {
		assert(profile.data is ProfileDataClassic)
		return profile.data as ProfileDataClassic
	}

	private fun requireLightleak(): ProfileDataLightleak {
		assert(profile.data is ProfileDataLightleak)
		return profile.data as ProfileDataLightleak
	}

	fun send(packet: IPacket): Boolean {
		if (packet is ProperPacket) {
			packet.returnIp = InetAddress.getLocalHost()
		}
		val address = InetAddress.getByName("255.255.255.255")
		val port = 6669
		val datagram = DatagramPacket(packet.serialize(), 256, address, port)
		val socket = DatagramSocket()
		socket.broadcast = true
		socket.send(datagram)
		return true
	}

	fun connectToStation(ssid: ByteArray, password: ByteArray) = WifiPacket(
		ssid = ssid,
		password = password,
		token = "1".toByteArray(),
	).send(this)

	fun findGadget(name: String) = DetectionPacket(
		requireLightleak(),
		requireLightleak().getGadget(name),
	).send(this)

	fun runStager() = CallbackPacket(
		requireLightleak(),
	).send(this)

	fun flashErase(offset: Int) = FlashErasePacket(
		requireLightleak(),
		offset,
	).send(this)

	fun flashWrite(offset: Int, data: ByteArray) = FlashWritePacket(
		requireLightleak(),
		offset,
		data,
	).send(this)

	fun flashRead(offset: Int, length: Int) = FlashReadPacket(
		requireLightleak(),
		offset,
		length,
	).send(this)
}
