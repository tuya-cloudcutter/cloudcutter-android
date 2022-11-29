/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.ext

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.getSystemService
import java.io.File
import java.text.DecimalFormat
import java.util.zip.CRC32
import kotlin.math.log10
import kotlin.math.pow


operator fun MatchResult.get(i: Int) = this.groupValues[i]

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

fun ByteArray.toHexString() = joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') }
fun Collection<Byte>.toHexString() = joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') }

fun ByteArray.crc32() = CRC32().let { crc ->
	crc.update(this@crc32)
	crc.value.toInt()
}

fun Collection<Byte>.crc32() = toByteArray().crc32()

fun Int.roundTo(value: Int): Int = this.div(value).times(value)

fun File.openChild(name: String) = File(this, name)

fun File.create(): File {
	if (!exists()) {
		parentFile?.mkdirs()
		createNewFile()
	}
	return this
}

fun Int.toReadableSize(unit: String = "iB", system: Double = 1024.0): String {
	if (this <= 0)
		return "-"
	val units = arrayOf("", "K", "M", "G", "T", "P")
	val digitGroups = (log10(this.toDouble()) / log10(system)).toInt()
	val value = DecimalFormat("#,##0.#")
		.format(this / system.pow(digitGroups.toDouble()))
	return "$value ${units[digitGroups]}$unit"
}
