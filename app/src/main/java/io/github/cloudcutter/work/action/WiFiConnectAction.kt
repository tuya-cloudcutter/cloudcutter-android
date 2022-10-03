/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text

class WiFiConnectAction(
	id: String,
	title: Text,
	nextId: String,
	val type: Type,
	val ssid: String?,
	val password: String? = null,
	timeout: Long = 60_000,
) : Action(id, title, nextId, timeout) {

	enum class Type {
		DEVICE_DEFAULT,
		DEVICE_CUSTOM,
		SSID,
	}
}
