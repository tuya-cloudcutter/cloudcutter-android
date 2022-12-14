/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-4.
 */

package io.github.cloudcutter.ext

import android.content.Context
import android.net.wifi.ScanResult
import android.util.Log
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import io.github.cloudcutter.work.exploit.event.WiFiScanResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "WifiExtensions"

suspend fun Context.wifiScan(): List<ScanResult> = suspendCancellableCoroutine { continuation ->
	Log.d(TAG, "Scanning networks")
	WifiUtils.withContext(this).scanWifi(continuation::resume).start()
}

suspend fun Context.wifiConnect(ssid: String, password: String?) =
	suspendCancellableCoroutine { continuation ->
		val builder = WifiUtils.withContext(this)
		continuation.invokeOnCancellation {
			builder.cancelAutoConnect()
		}
		Log.d(TAG, "Connecting to $ssid / $password")
		builder.connectWith(ssid, password ?: "")
			.onConnectionResult(object : ConnectionSuccessListener {
				override fun success() {
					Log.d(TAG, "Connected")
					continuation.resume(null)
				}

				override fun failed(errorCode: ConnectionErrorCode) {
					Log.d(TAG, "Connection failed: $errorCode")
					continuation.resume(errorCode.toString())
				}
			}).start()
	}

fun ScanResult.hasEncryption(): Boolean {
	if (capabilities.contains("WEP")) {
		return true
	} else if (capabilities.contains("PSK")) {
		return true
	} else if (capabilities.contains("EAP")) {
		return true
	}
	return false
}

fun List<ScanResult>.toEvent() = WiFiScanResponse(this.map {
	WiFiScanResponse.Network(
		ssid = it.SSID,
		bssid = it.BSSID,
		isEncrypted = it.hasEncryption(),
	)
})
