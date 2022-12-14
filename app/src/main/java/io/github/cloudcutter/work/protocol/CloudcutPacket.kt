/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol

import io.github.cloudcutter.data.model.ProfileClassic
import io.github.cloudcutter.ext.getFinishToken
import io.github.cloudcutter.work.protocol.base.BasePacket

data class CloudcutPacket(
	val data: ProfileClassic.Data,
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
			"token" to getFinishToken(data.addressFinish),
			"passwd" to passwd,
		)
	}

	override fun getCommand(): ByteArray? = null
	override fun getOptions(): ByteArray? = null
}
