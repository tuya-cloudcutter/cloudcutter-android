/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

package io.github.cloudcutter.work.event

class WiFiConnectRequest(
	val ssid: String,
	val password: String?,
) : Event()
