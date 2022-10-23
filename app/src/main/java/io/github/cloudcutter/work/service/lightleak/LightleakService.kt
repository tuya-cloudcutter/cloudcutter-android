/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-7.
 */

package io.github.cloudcutter.work.service.lightleak

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.util.Log
import com.hadilq.liveevent.LiveEvent
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.ext.crc32
import io.github.cloudcutter.ext.toHexString
import io.github.cloudcutter.work.protocol.proper.FlashReadPacket
import io.github.cloudcutter.work.protocol.send
import io.github.cloudcutter.work.service.lightleak.command.CommandRequest
import io.github.cloudcutter.work.service.lightleak.command.CommandResponse
import io.github.cloudcutter.work.service.lightleak.command.KeyblockReadCommand
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import java.net.InetAddress
import kotlin.math.ceil
import kotlin.math.min

class LightleakService : Service(), CoroutineScope {
	companion object {
		private const val TAG = "LightleakService"
	}

	override val coroutineContext = Job() + Dispatchers.IO
	private val bus = Bus()
	private val packets = LiveEvent<Pair<Int, ByteArray>>()
	private lateinit var profile: ProfileLightleak.Data
	private lateinit var returnIp: InetAddress

	private var newRequestId = 1
		get() {
			field++
			return field
		}

	inner class ServiceBinder : Binder() {
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
			Log.d(TAG, "Got packet: requestId=$requestId, crc32=$crc32, data=${data.take(64).toHexString()}")
			if (crc32 == data.crc32()) {
				packets.postValue(requestId to data)
			} else {
				Log.e(TAG, "Packet CRC invalid. Got $crc32, expected ${data.crc32()}")
			}
		}
	}

	@Subscribe
	fun onCommand(command: CommandRequest) = launch {
		Log.d(TAG, "Running command: $command")
		val response = when (command) {
			is KeyblockReadCommand -> readKeyblock()
			else -> null
		} ?: return@launch
		bus.post(CommandResponse(command, response))
	}

	private suspend fun flashRead(start: Int, end: Int): List<ByteArray> {
		var offset = start
		val readBlockSize = 0x1000
		val readPacketSize = 1024f
		val data = mutableListOf<ByteArray>()
		while (offset < end) {
			val requestId = newRequestId
			val readLength = min(readBlockSize, end - offset)
			val readCount = ceil(readLength / readPacketSize).toInt()
			// read flash
			FlashReadPacket(
				profile = profile,
				requestId = requestId,
				offset = offset,
				length = readLength,
				maxLength = readPacketSize.toInt(),
			).also { it.returnIp = returnIp }.send("192.168.175.1")
			// wait for response
			data += packets.awaitCount(requestId, count = readCount)
			// increment reading address
			offset += readLength
		}
		return data
	}

	private suspend fun readKeyblock(): List<ByteArray> {
		val offset = 0x200000 - 0x3000 - 0xE000 - 0x1000
		val length = 0x1000 + 0xE000 // incl. encrypted key, excl. swap
		return flashRead(offset, offset + length)
	}
}
