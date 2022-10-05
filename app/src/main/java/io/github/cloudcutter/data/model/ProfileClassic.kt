/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-4.
 */

package io.github.cloudcutter.data.model

import com.mikepenz.iconics.typeface.IIcon
import com.squareup.moshi.Json

class ProfileClassic(
	slug: String,
	name: String,
	@Json(name = "sub_name") subName: String?,
	type: Type,
	icon: IIcon?,
	@Json(name = "github_issues") githubIssues: List<Int>,
	devices: List<DeviceBase>,
	firmware: Firmware?,
	data: Data,
) : Profile<ProfileClassic.Data>(
	slug,
	name,
	subName,
	type,
	icon,
	githubIssues,
	devices,
	firmware,
	data,
) {

	data class Data(
		@Json(name = "address_finish") val addressFinish: Int,
		@Json(name = "address_ssid") val addressSsid: Int?,
		@Json(name = "address_passwd") val addressPasswd: Int?,
		@Json(name = "address_datagram") val addressDatagram: Int?,
	)
}
