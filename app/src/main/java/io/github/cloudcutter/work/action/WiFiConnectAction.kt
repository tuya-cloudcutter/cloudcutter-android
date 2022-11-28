/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.R
import io.github.cloudcutter.util.Text
import kotlinx.coroutines.TimeoutCancellationException

class WiFiConnectAction(
	id: String,
	title: Text,
	nextId: String,
	val type: Type,
	val ssid: String?,
	val password: String? = null,
	timeout: Long = 10 * 60_000,
) : Action(id, title, nextId, timeout, mapOf(
	TimeoutCancellationException::class.java to Text(R.string.message_error_wifi_connect_timeout),
)) {

	enum class Type {
		DEVICE_DEFAULT,
		DEVICE_CUSTOM,
		SSID,
	}
}
