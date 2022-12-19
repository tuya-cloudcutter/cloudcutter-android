/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-7.
 */

package io.github.cloudcutter.work.lightleak

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.util.Log
import io.github.cloudcutter.ext.crc32
import io.github.cloudcutter.ext.getBroadcastAddress
import io.github.cloudcutter.ext.send
import io.github.cloudcutter.util.RequestBus
import io.github.cloudcutter.work.exceptions.PacketReadException
import io.github.cloudcutter.work.exceptions.ServiceDisconnectedException
import io.github.cloudcutter.work.lightleak.command.CommandRequest
import io.github.cloudcutter.work.lightleak.command.FlashReadCommand
import io.github.cloudcutter.work.protocol.proper.FlashReadPacket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.ceil

class LightleakService : Service(), CoroutineScope {
	companion object {
		private const val TAG = "LightleakService"
	}

	override val coroutineContext = Job() + Dispatchers.IO
	private val bus = RequestBus<CommandRequest>()
	private val packets = Channel<Pair<Int, ByteArray>>(Channel.UNLIMITED)

	private var data: LightleakData? = null
	private val log
		get() = data?.serviceLog

	private var newRequestId = 1
		get() {
			field++
			return field
		}

	inner class ServiceBinder : Binder() {
		fun setData(data: LightleakData) {
			this@LightleakService.data = data
		}

		suspend fun <T : CommandRequest, D> request(command: T) = bus.request<D>(command)
	}

	override fun onCreate() {
		Log.d(TAG, "Creating $TAG")
		bus.subscribe(this, this::onCommand)
		startUdpListener()
	}

	override fun onBind(intent: Intent) = ServiceBinder()

	override fun onUnbind(intent: Intent): Boolean {
		this@LightleakService.data = null
		return false
	}

	override fun onDestroy() {
		Log.d(TAG, "Destroying $TAG")
		this.cancel()
	}

	private fun startUdpListener() = launch {
		val selectorManager = SelectorManager(Dispatchers.IO)
		val address = InetSocketAddress("0.0.0.0", 6667)
		val socket = aSocket(selectorManager).udp().bind(address) {
			reuseAddress = true
			broadcast = true
		}
		Log.d(TAG, "UDP listener started")

		while (isActive) {
			val packet = socket.receive().packet
			val requestId = packet.readIntLittleEndian()
			val crc32 = packet.readIntLittleEndian()
			val data = packet.readBytes()
			// val dataString = data.take(64).toHexString()
			// Log.d(TAG, "Got packet: requestId=$requestId, crc32=$crc32, data=$dataString")
			if (crc32 == data.crc32()) {
				packets.send(requestId to data)
			} else {
				log?.invoke("Packet CRC invalid. Got $crc32, expected ${data.crc32()}")
			}
		}
	}

	private suspend fun onCommand(command: CommandRequest): Any {
		log?.invoke("Running command: $command")
		return when (command) {
			is FlashReadCommand -> flashRead(
				start = command.offset,
				end = command.offset + command.length,
				output = command.output,
			)
			else -> throw NotImplementedError("Unknown command $command")
		}
	}

	private suspend fun flashRead(start: Int, end: Int, output: File): List<ByteArray> {
		val readPacketSize = 1024
		val readPacketsMax = 8
		val readTimeoutMs = 10

		val size = end - start
		val packetCount = ceil(size / readPacketSize.toFloat()).toInt()
		val packetList = MutableList<ByteArray?>(packetCount) { null }
		val packetOffsets = MutableList(packetCount) { start + readPacketSize * it }

		var pauseCount = 0
		var failedCount = 0
		var exception: Exception? = null
		while (isActive) {
			val readStartIndex = packetList.indexOf(null)
			if (readStartIndex == -1)
				break
			val readEndIndex = packetList
				.subList(readStartIndex + 1, packetCount)
				.indexOfFirst { it != null }

			val readPacketCount = when (readEndIndex) {
				-1 -> packetCount - readStartIndex
				else -> readEndIndex + 1
			}.coerceAtMost(readPacketsMax)

			val requestId = newRequestId
			val readOffset = packetOffsets[readStartIndex]
			// read flash
			log?.invoke("Reading data #$readStartIndex, offset=0x${readOffset.toString(16)}, count=$readPacketCount")
			val packet = FlashReadPacket(
				profile = data?.profileData ?: throw ServiceDisconnectedException(),
				requestId = requestId,
				offset = readOffset,
				length = readPacketCount * readPacketSize,
				maxLength = readPacketSize,
			).also { it.returnIp = data?.localAddress ?: getBroadcastAddress() }
			try {
				packet.send(data?.gatewayAddress ?: getBroadcastAddress())
			} catch (e: Throwable) {
				// wait a bit longer in case of [network] exceptions
				delay(500)
			}

			// wait a moment
			if (pauseCount++ == 100)
				delay(500)
//			else
//				delay(20)

			// receive all currently buffered packets
			var packetsReceived = 0
			val endTime = System.currentTimeMillis() + readTimeoutMs
			while (packetsReceived < readPacketsMax && System.currentTimeMillis() < endTime) {
				val result = packets.tryReceive()
				if (!result.isSuccess)
					continue
				val bytes = result.getOrThrow().second
				val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
				val offset = buf.int
				val length = buf.int
				val index = packetOffsets.indexOf(offset)
				log?.invoke("Received data #$index, offset=0x${offset.toString(16)}, size=${bytes.size}")
				packetList[index] = bytes.sliceArray(8 until length + 8)
				packetsReceived++
			}

			// check if all packets were received
			val progress = packetList.count { it != null }
			if (packetsReceived > 0) {
				data?.progressValue?.postValue(progress * 100 / packetCount)
				data?.progressBytes?.postValue(progress * readPacketSize)
				failedCount = 0
			} else if (failedCount++ >= 200) {
				exception = PacketReadException()
				break
			}
			if (progress == packetCount)
				break
		}
		data?.progressValue?.postValue(null)
		data?.progressBytes?.postValue(null)

		withContext(Dispatchers.IO) {
			RandomAccessFile(output, "rw").use {
				packetList.forEachIndexed { index, chunk ->
					chunk ?: return@forEachIndexed
					it.seek((start + index * readPacketSize).toLong())
					it.write(chunk)
				}
			}
		}

		if (exception != null)
			throw exception

		return packetList.filterNotNull()
	}
}
