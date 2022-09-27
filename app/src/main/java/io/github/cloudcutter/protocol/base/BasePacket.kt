/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.protocol.base

import io.github.cloudcutter.protocol.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

abstract class BasePacket : IPacket {

	override fun serialize(): ByteArray {
		val buf = ByteBuffer.allocate(DGRAM_SIZE)
		buf.order(ByteOrder.BIG_ENDIAN)
		buf.putInt(DGRAM_HEAD)
		buf.putInt(DGRAM_FR_NUM)
		buf.putInt(DGRAM_FR_TYPE)
		buf.mark()
		buf.putInt(0x00) // length

		val jsonStart = buf.position()
		val charsGlue = "\":\"".toByteArray()
		val charsEscaped = "\"\\".toByteArray()
		var first = true
		buf.put('{'.code.toByte())
		for ((key, value) in getJsonFields()) {
			if (!first)
				buf.put(','.code.toByte())
			first = false

			buf.put('"'.code.toByte())
			buf.put(key.toByteArray())
			buf.put(charsGlue)
			for (byte in value) {
				if (byte == 0x00.toByte())
					throw IllegalArgumentException("JSON field may not contain null bytes")
				if (byte in charsEscaped)
					buf.put('\\'.code.toByte())
				buf.put(byte)
			}
			buf.put('"'.code.toByte())
		}
		buf.put('}'.code.toByte())
		val jsonEnd = buf.position()

		buf.reset()
		buf.putInt(jsonEnd - jsonStart + 8) // payload + crc + tail
		buf.position(jsonEnd)

		val crc = CRC32()
		crc.update(buf.array().take(buf.position()).toByteArray())
		buf.putInt(crc.value.toInt())
		buf.putInt(DGRAM_TAIL)

		buf.order(ByteOrder.LITTLE_ENDIAN)

		val command = getCommand()
		val options = getOptions()

		if (command != null) {
			if (buf.position() > getCommandOffset())
				throw IllegalArgumentException("Payload too long to store a command")
			if (command.size > getOptionsOffset() - getCommandOffset())
				throw IllegalArgumentException("Command too long")
			if (options == null)
				throw IllegalArgumentException("Specified command with no options")
			buf.position(getCommandOffset())
			buf.put(command)
		}

		if (options != null) {
			if (options.size != OPT_LENGTH)
				throw IllegalArgumentException("Invalid options length")
			buf.position(getOptionsOffset())
			buf.put(options)
			buf.putInt(OPT_MARKER)
		}

		return buf.array()
	}
}
