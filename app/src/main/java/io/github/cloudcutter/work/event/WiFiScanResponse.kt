/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

package io.github.cloudcutter.work.event

class WiFiScanResponse(
	val networks: List<Network>,
) : Event {

	data class Network(
		val ssid: String,
		val bssid: String,
		val isEncrypted: Boolean,
	)
}
