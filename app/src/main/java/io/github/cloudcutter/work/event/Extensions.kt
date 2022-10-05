/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

@file:Suppress("ControlFlowWithEmptyBody")

package io.github.cloudcutter.work.event

import android.net.wifi.ScanResult
import io.github.cloudcutter.ext.hasEncryption
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout

suspend inline fun <reified T : Event> Channel<Event>.await(): T {
	while (this.tryReceive().getOrNull() != null);
	while (true) {
		val event = receive()
		if (event is T) return event
	}
}

suspend inline fun <reified T : Event> Channel<Event>.awaitTimeout(timeout: Long): T? {
	while (this.tryReceive().getOrNull() != null);
	while (true) {
		try {
			val event = withTimeout(timeout) { receive() }
			if (event is T) return event
		} catch (e: TimeoutCancellationException) {
			return null
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
