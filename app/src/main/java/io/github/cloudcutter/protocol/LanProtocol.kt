/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.protocol

import io.github.cloudcutter.data.ClassicProfile
import io.github.cloudcutter.data.FlashBasedProfile
import io.github.cloudcutter.data.IProfile
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
	val profile: IProfile,
) {

	fun requireClassic(): ClassicProfile {
		assert(profile is ClassicProfile)
		return profile as ClassicProfile
	}

	fun requireFlashBased(): FlashBasedProfile {
		assert(profile is FlashBasedProfile)
		return profile as FlashBasedProfile
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
		requireFlashBased(),
		requireFlashBased().getGadget(name),
	).send(this)

	fun runStager() = CallbackPacket(
		requireFlashBased(),
	).send(this)

	fun flashErase(offset: Int) = FlashErasePacket(
		requireFlashBased(),
		offset,
	).send(this)

	fun flashWrite(offset: Int, data: ByteArray) = FlashWritePacket(
		requireFlashBased(),
		offset,
		data,
	).send(this)

	fun flashRead(offset: Int, length: Int) = FlashReadPacket(
		requireFlashBased(),
		offset,
		length,
	).send(this)
}
