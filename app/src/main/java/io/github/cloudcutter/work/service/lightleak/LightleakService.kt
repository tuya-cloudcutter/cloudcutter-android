/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-7.
 */

package io.github.cloudcutter.work.service.lightleak

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.ext.crc32
import io.github.cloudcutter.ext.toHexString
import io.github.cloudcutter.work.protocol.proper.FlashReadPacket
import io.github.cloudcutter.work.protocol.send
import io.github.cloudcutter.work.service.lightleak.command.CommandRequest
import io.github.cloudcutter.work.service.lightleak.command.CommandResponse
import io.github.cloudcutter.work.service.lightleak.command.FlashReadCommand
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.ceil
import kotlin.math.min

class LightleakService : Service(), CoroutineScope {
	companion object {
		private const val TAG = "LightleakService"
	}

	override val coroutineContext = Job() + Dispatchers.IO
	private val bus = Bus()
	private val packets = Channel<Pair<Int, ByteArray>>(Channel.UNLIMITED)
	private val progress = MutableLiveData<Int?>(null)
	private lateinit var profile: ProfileLightleak.Data
	private lateinit var returnIp: InetAddress

	private var newRequestId = 1
		get() {
			field++
			return field
		}

	inner class ServiceBinder : Binder() {
		val progress
			get() = this@LightleakService.progress

		fun setProfile(data: ProfileLightleak.Data) {
			profile = data
		}

		fun setReturnIp(ip: InetAddress) {
			returnIp = ip
		}

		suspend fun <T : CommandRequest, D> execute(command: T): D {
			Log.d(TAG, "Sending: $command")
			bus.post(command)
			Log.d(TAG, "Awaiting response: $command")
			val response = bus.awaitResponse<T, D>(command)
			Log.d(TAG, "Got response: $command")
			return response.data
		}
	}

	override fun onCreate() {
		Log.d(TAG, "Creating $TAG")
		bus.register(this)
		startUdpListener()
	}

	override fun onBind(intent: Intent) = ServiceBinder()

	override fun onDestroy() {
		Log.d(TAG, "Destroying $TAG")
		bus.unregister(this)
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

		while (true) {
			val packet = socket.receive().packet
			val requestId = packet.readIntLittleEndian()
			val crc32 = packet.readIntLittleEndian()
			val data = packet.readBytes()
			val dataString = data.take(64).toHexString()
			Log.d(TAG, "Got packet: requestId=$requestId, crc32=$crc32, data=$dataString")
			if (crc32 == data.crc32()) {
				packets.send(requestId to data)
			} else {
				Log.e(TAG, "Packet CRC invalid. Got $crc32, expected ${data.crc32()}")
			}
		}
	}

	@Subscribe
	fun onCommand(command: CommandRequest) = launch {
		Log.d(TAG, "Running command: $command")
		val response = when (command) {
			is FlashReadCommand -> flashRead(
				start = command.offset,
				end = command.offset + command.length,
				output = command.output,
			)
			else -> null
		} ?: return@launch
		withContext(Dispatchers.Main) {
			bus.post(CommandResponse(command, response))
		}
	}

	private suspend fun flashRead(start: Int, end: Int, output: DocumentFile): List<ByteArray> {
		val readBlockSize = 0x4000
		val readPacketSize = 1024

		val size = end - start
		val packetCount = ceil(size / readPacketSize.toFloat()).toInt()
		val packetList = MutableList<ByteArray?>(packetCount) { null }
		val packetOffsets = MutableList(packetCount) { start + readPacketSize * it }

		var pauseCount = 0
		while (true) {
			val readIndex = packetList.indexOfFirst { it == null }
			if (readIndex == -1)
				break

			val requestId = newRequestId
			// get offset for the index and calculate read length
			val readOffset = packetOffsets[readIndex]
			val readLength = min(readBlockSize, end - readOffset)
			// read flash
			Log.d(TAG, "Reading data #$readIndex, offset=0x${readOffset.toString(16)}")
			FlashReadPacket(
				profile = profile,
				requestId = requestId,
				offset = readOffset,
				length = readLength,
				maxLength = readPacketSize,
			).also { it.returnIp = returnIp }.send("192.168.175.1")

			// wait a moment
			if (pauseCount++ == 100)
				delay(2000)
			else
				delay(20)

			// receive all currently buffered packets
			while (true) {
				val result = packets.tryReceive()
				if (!result.isSuccess)
					break
				val bytes = result.getOrThrow().second
				val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
				val offset = buf.int
				val length = buf.int
				val index = packetOffsets.indexOf(offset)
				Log.d(TAG,
					"Received data #$index, offset=0x${offset.toString(16)}, size=${bytes.size}")
				packetList[index] = bytes.sliceArray(8 until length + 8)
			}

			// check if all packets were received
			val progress = packetList.count { it != null }
			this.progress.postValue(progress * 100 / packetCount)
			if (progress == packetCount)
				break
		}
		progress.postValue(null)

		withContext(Dispatchers.IO) {
			contentResolver.openFileDescriptor(output.uri, "rw").use { pfd ->
				val stream = FileOutputStream(pfd?.fileDescriptor ?: return@use)
				val channel = stream.channel
				channel.position(start.toLong())
				for (chunk in packetList) {
					if (chunk == null)
						continue
					channel.write(ByteBuffer.wrap(chunk))
				}
			}
		}

		return packetList.filterNotNull()
	}
}
