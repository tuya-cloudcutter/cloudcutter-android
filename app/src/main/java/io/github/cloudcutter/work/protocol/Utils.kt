/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol

import io.github.cloudcutter.work.protocol.base.IPacket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

fun getFinishToken(address: Int) = buildByteArray(
	size = 76,
	"A".repeat(72),
	address,
).take(75).toByteArray()

fun Int.toOffset(): Int = this.floorDiv(32) * 34 + this.mod(32)
fun Int.toAddress(): Int = this.floorDiv(34) * 32 + this.mod(34)

suspend fun IPacket.send(address: String) {
	val selectorManager = SelectorManager(Dispatchers.IO)
	val socket = aSocket(selectorManager).udp().connect(
		remoteAddress = InetSocketAddress(address, 6669),
		localAddress = null,
		configure = {
			broadcast = true
		},
	)
	val send = socket.openWriteChannel(autoFlush = true)
	val packet = this.serialize()
	withContext(Dispatchers.IO) {
		send.writeFully(packet)
		// Log.d("IPacket", "Wrote packet: ${packet.toHexString()}")
		socket.close()
		selectorManager.close()
	}
}
