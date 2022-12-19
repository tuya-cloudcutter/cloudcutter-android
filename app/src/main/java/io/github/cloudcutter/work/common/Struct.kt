/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-19.
 */

package io.github.cloudcutter.work.common

import java.nio.ByteBuffer
import java.nio.ByteOrder

object Struct {

	private fun String.getEndianness() = when (this[0]) {
		'<' -> ByteOrder.LITTLE_ENDIAN
		'>', '!' -> ByteOrder.BIG_ENDIAN
		else -> ByteOrder.nativeOrder()
	}

	private fun String.getFormatList() = this
		.replace(" ", "")
		.replace("""(\d+[^s\d])""".toRegex()) {
			it.value
				.last()
				.toString()
				.repeat(it.value.dropLast(1).toInt())
		}
		.replace("""\$\d+s|\d+s""".toRegex()) { " ${it.value} " }
		.split(" ")
		.filter { it.isNotBlank() }
		.flatMap { if (it.last() == 's') listOf(it) else it.split("") }
		.map { if (it == "s") "1s" else it }
		.filter { it.isNotBlank() }
		.dropWhile { it[0] in "@=<>!" }

	private fun getLengthFields(format: List<String>): Map<Int, Int> {
		// Map<length field, data field>
		val lengthFields = mutableMapOf<Int, Int>()

		// find all dynamically-sized strings
		var nullCount = 0
		for ((index, field) in format.withIndex()) {
			if (field == "x") {
				nullCount++
				continue
			}
			if (!field.startsWith("$"))
				continue
			val fieldIndex = field.drop(1).dropLast(1).toInt()
			lengthFields[fieldIndex] = index - nullCount
		}
		return lengthFields
	}

	fun pack(formatString: String, valuesRaw: List<Any>): ByteArray {
		val endianness = formatString.getEndianness()
		val format = formatString.getFormatList()
		val lengthFields = getLengthFields(format)

		// replace 'length fields' with actual length
		val values = valuesRaw.mapIndexed { index, value ->
			if (index in lengthFields)
				(valuesRaw[lengthFields[index] ?: 0] as ByteArray).size
			else
				value
		}

		var nullCount = 0
		val totalLength = format.mapIndexed { index, field ->
			if (field == "x")
				nullCount++
			when (field.last()) {
				'x' -> 1
				'c', 'b', 'B' -> 1
				'?' -> 1
				'h', 'H' -> 2
				'i', 'I' -> 4
				'l', 'L' -> 4
				'q', 'Q' -> 8
				'f' -> 4
				'd' -> 8
				's' -> if (field[0] == '$')
					(values[index - nullCount] as ByteArray).size
				else
					field.dropLast(1).toInt()
				'S' -> (values[index - nullCount] as ByteArray).size
				else -> throw UnsupportedOperationException("Format specifier $field not supported")
			}
		}.sum()

		val buf = ByteBuffer.allocate(totalLength).order(endianness)
		nullCount = 0
		for ((index, field) in format.withIndex()) {
			val spec = field.last()
			if (spec == 'x') {
				nullCount++
				buf.put(0)
				continue
			}
			val value = values[index - nullCount]
			println("Packing field $index - $field($spec) - $value")
			when (spec) {
				'c' -> buf.putChar(value as Char)
				'b', 'B' -> buf.put((value as Number).toByte())
				'?' -> buf.put(if (value == true) 1 else 0)
				'h', 'H' -> buf.putShort((value as Number).toShort())
				'i', 'I' -> buf.putInt((value as Number).toInt())
				'l', 'L' -> buf.putInt((value as Number).toInt())
				'q', 'Q' -> buf.putLong((value as Number).toLong())
				'f' -> buf.putFloat((value as Number).toFloat())
				'd' -> buf.putDouble((value as Number).toDouble())
				's' -> {
					val size = (value as ByteArray).size
					if (field[0] == '$') {
						buf.put(value)
					} else {
						val fieldSize = field.dropLast(1).toInt()
						when {
							size > fieldSize -> buf.put(value.take(fieldSize).toByteArray())
							size < fieldSize -> {
								buf.put(value)
								buf.put(ByteArray(fieldSize - size) { 0 })
							}
							else -> buf.put(value)
						}
					}
				}
				'S' -> buf.put(value as ByteArray)
				else -> throw UnsupportedOperationException("Format specifier $field not supported")
			}
		}

		return buf.array()
	}

	fun unpack(formatString: String, data: ByteArray): List<Any> {
		val endianness = formatString.getEndianness()
		val format = formatString.getFormatList()
		val values = mutableListOf<Any>()

		val buf = ByteBuffer.wrap(data).order(endianness)
		for ((index, field) in format.withIndex()) {
			val spec = field.last()
			print("Unpacking field $index - $field($spec) - ")
			@Suppress("UsePropertyAccessSyntax")
			val value = when (spec) {
				'x' -> buf.get()
				'c' -> buf.getChar()
				'b' -> buf.get().toInt()
				'B' -> buf.get().toUByte().toInt()
				'?' -> buf.get().toInt() == 1
				'h' -> buf.getShort().toInt()
				'H' -> buf.getShort().toUShort().toInt()
				'i' -> buf.getInt()
				'I' -> buf.getInt().toUInt().toInt()
				'l' -> buf.getInt().toLong()
				'L' -> buf.getInt().toUInt().toLong()
				'q' -> buf.getLong()
				'Q' -> buf.getLong().toULong().toLong()
				'f' -> buf.getFloat()
				'd' -> buf.getDouble()
				's' -> {
					val size = if (field[0] == '$') {
						val fieldIndex = field.drop(1).dropLast(1).toInt()
						(values[fieldIndex] as Number).toInt()
					} else {
						field.dropLast(1).toInt()
					}
					val byteArray = ByteArray(size)
					buf.get(byteArray)
					byteArray
				}
				'S' -> data.sliceArray(buf.position() until data.size)
				else -> throw UnsupportedOperationException("Format specifier $field not supported")
			}
			println(value)
			if (spec == 'x')
				continue
			values.add(value)
		}

		return values
	}
}
