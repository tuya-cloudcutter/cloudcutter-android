/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.ext

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

fun ByteArray.toHexString() = joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') }
fun Collection<Byte>.toHexString() = joinToString(" ") { byte ->
	byte.toUByte()
		.toString(16)
		.padStart(2, '0')
}

fun ByteArray.crc32() = CRC32().let { crc ->
	crc.update(this@crc32)
	crc.value.toInt()
}

fun Collection<Byte>.crc32() = toByteArray().crc32()

fun getFinishToken(address: Int) = buildByteArray(
	size = 76,
	"A".repeat(72),
	address,
).take(75).toByteArray()

fun Int.toOffset(): Int = this.floorDiv(32) * 34 + this.mod(32)
fun Int.toAddress(): Int = this.floorDiv(34) * 32 + this.mod(34)

fun buildByteArray(size: Int, vararg items: Any): ByteArray {
	val buf = ByteBuffer.allocate(size)
	buf.order(ByteOrder.LITTLE_ENDIAN)
	for (item in items) {
		when (item) {
			is String -> buf.put(item.toByteArray())
			is Int -> buf.putInt(item)
			is Float -> buf.putInt(item.toInt())
			is Long -> buf.putInt(item.toInt())
			is ByteArray -> buf.put(item)
			is Char -> buf.put(item.code.toByte())
			else -> throw IllegalArgumentException("Invalid argument type")
		}
	}
	return buf.array()
}
