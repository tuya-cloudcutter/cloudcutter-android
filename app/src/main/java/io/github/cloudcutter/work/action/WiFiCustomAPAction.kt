/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

class WiFiCustomAPAction(
	id: String,
	title: Text,
	nextId: String,
	val ssid: ByteArray,
	val password: ByteArray,
	val stopTimeout: Int,
	timeout: Long = 10_000,
) : Action(id, title, nextId, timeout) {

	fun buildPacket(): ByteArray {
		val packet = ByteBuffer.allocate(33 + 63 + 4)
		packet.order(ByteOrder.LITTLE_ENDIAN)
		packet.put(ssid)
		packet.position(33)
		packet.put(password)
		packet.position(33 + 63)
		packet.putInt(stopTimeout)
		val packetData = packet.array()
		val crc = CRC32()
		crc.update(packetData)

		val buf = ByteBuffer.allocate(4 + 1 + packetData.size + 4)
		buf.order(ByteOrder.LITTLE_ENDIAN)
		buf.put("cctr".toByteArray())   // magic
		buf.put(104.toByte())           // length
		buf.put(packetData)             // packet data
		buf.putInt(crc.value.toInt())   // crc32 of packet data
		return buf.array()
	}
}
