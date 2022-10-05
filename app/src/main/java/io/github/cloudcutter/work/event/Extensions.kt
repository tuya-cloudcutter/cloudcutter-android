/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

@file:Suppress("ControlFlowWithEmptyBody")

package io.github.cloudcutter.work.event

import android.net.wifi.ScanResult
import android.util.Log
import io.github.cloudcutter.ext.hasEncryption
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout

suspend inline fun <reified T : Event> Channel<Event>.await(): T {
	Log.d("Extensions", "Awaiting ${T::class.java.simpleName}")
	while (this.tryReceive().getOrNull() != null);
	while (true) {
		val event = receive()
		if (event is T) {
			Log.d("Extensions", "Event: $event")
			return event
		}
	}
}

suspend inline fun <reified T : Event> Channel<Event>.awaitTimeout(timeout: Long): T {
	Log.d("Extensions", "Awaiting ${T::class.java.simpleName} for $timeout ms")
	while (this.tryReceive().getOrNull() != null);
	while (true) {
		val event = withTimeout(timeout) { receive() }
		if (event is T) {
			Log.d("Extensions", "Event: $event")
			return event
		}
	}
}

fun List<ScanResult>.toEvent() = WiFiScanResponse(this.map {
	WiFiScanResponse.Network(
		ssid = it.SSID,
		bssid = it.BSSID,
		isEncrypted = it.hasEncryption(),
	)
})
