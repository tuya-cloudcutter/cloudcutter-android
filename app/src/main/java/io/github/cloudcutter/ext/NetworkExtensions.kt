/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-14.
 */

package io.github.cloudcutter.ext

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.getSystemService
import io.github.cloudcutter.work.protocol.base.IPacket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address

fun Context.hasInternet(): Boolean {
	val cm = getSystemService<ConnectivityManager>()
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		return cm?.getNetworkCapabilities(cm.activeNetwork)
			?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
	} else {
		when (cm?.activeNetworkInfo?.type) {
			ConnectivityManager.TYPE_WIFI -> return true
			ConnectivityManager.TYPE_MOBILE -> return true
			ConnectivityManager.TYPE_VPN -> return true
		}
	}
	return false
}

fun String.toInet4Address() =
	Inet4Address.getByAddress(this.split(".").map { it.toByte() }.toByteArray()) as Inet4Address

fun Inet4Address.toInet4String() =
	this.address.joinToString(".") { it.toUByte().toString() }

fun getBroadcastAddress() = "255.255.255.255".toInet4Address()
fun getLocalhostAddress() = "127.0.0.1".toInet4Address()

suspend fun IPacket.send(address: Inet4Address) {
	val selectorManager = SelectorManager(Dispatchers.IO)
	val socket = aSocket(selectorManager).udp().connect(
		remoteAddress = InetSocketAddress(address.toInet4String(), 6669),
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
