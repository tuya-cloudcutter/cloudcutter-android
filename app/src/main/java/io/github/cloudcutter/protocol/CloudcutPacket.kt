/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.protocol

import io.github.cloudcutter.protocol.base.BasePacket

data class CloudcutPacket(
	val finishAddress: Int,
	val targetAddress: Int,
	val intermediateAddress: Int? = null,
	val apSsid: String = "B",
	val newUuid: String = "abcd",
	val newAuthKey: String = "",
	val newPskKey: String = "",
) : BasePacket() {

	override fun getJsonFields(): Map<String, ByteArray> {
		val ssid = "A".toByteArray()
		val passwd = "A".toByteArray()

		return mapOf(
			"auzkey" to newAuthKey.toByteArray(),
			"uuid" to newUuid.toByteArray(),
			"pskKey" to newPskKey.toByteArray(),
			"ap_ssid" to apSsid.toByteArray(),
			"ssid" to ssid,
			"token" to getFinishToken(finishAddress),
			"passwd" to passwd,
		)
	}

	override fun getCommand(): ByteArray? = null
	override fun getOptions(): ByteArray? = null
}
