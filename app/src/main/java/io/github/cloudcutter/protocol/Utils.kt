/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.protocol

import io.github.cloudcutter.protocol.base.IPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun buildByteArray(size: Int, vararg items: Any): ByteArray {
	val buf = ByteBuffer.allocate(size)
	buf.order(ByteOrder.LITTLE_ENDIAN)
	for (item in items) {
		when (item) {
			is String -> buf.put(item.toByteArray())
			is Int -> buf.putInt(item)
			is Long -> buf.putInt(item.toInt())
			is ByteArray -> buf.put(item)
			is Char -> buf.put(item.code.toByte())
			else -> throw IllegalArgumentException("Invalid argument type")
		}
	}
	return buf.array()
}

fun getFinishToken(address: Int) = buildByteArray(
	size = 76,
	"A".repeat(72),
	address,
).take(75).toByteArray()

fun IPacket.send(protocol: LanProtocol) = protocol.send(this)
