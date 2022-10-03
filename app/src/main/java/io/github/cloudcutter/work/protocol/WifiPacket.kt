/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol

import io.github.cloudcutter.work.protocol.base.BasePacket

data class WifiPacket(
	val ssid: ByteArray,
	val password: ByteArray,
	val token: ByteArray,
) : BasePacket() {

	override fun getJsonFields() = mapOf(
		"ssid" to ssid,
		"passwd" to password,
		"token" to token,
	)

	override fun getCommand(): ByteArray? = null
	override fun getOptions(): ByteArray? = null
}
