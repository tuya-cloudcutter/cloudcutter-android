/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.R
import io.github.cloudcutter.util.Text
import kotlinx.coroutines.TimeoutCancellationException

class WiFiScanAction(
	id: String,
	title: Text,
	nextId: String,
	val ssid: String,
	timeout: Long = 10_000,
) : Action(id, title, nextId, timeout, mapOf(
	TimeoutCancellationException::class.java to Text(R.string.message_error_wifi_scan_timeout, ssid),
))
