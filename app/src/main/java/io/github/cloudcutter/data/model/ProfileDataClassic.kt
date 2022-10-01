/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.data.model

import com.squareup.moshi.Json

data class ProfileDataClassic(
	@Json(name = "address_finish")
	val addressFinish: Int,
	@Json(name = "address_ssid")
	val addressSsid: Int?,
	@Json(name = "address_passwd")
	val addressPasswd: Int?,
	@Json(name = "address_datagram")
	val addressDatagram: Int?,
) : ProfileData
