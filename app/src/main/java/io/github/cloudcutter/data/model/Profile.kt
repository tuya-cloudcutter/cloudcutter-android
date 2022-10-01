/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.model

import com.mikepenz.iconics.typeface.IIcon
import com.squareup.moshi.Json

class Profile(
	slug: String,
	name: String,
	@Json(name = "sub_name")
	subName: String?,
	type: Type,
	icon: IIcon?,

	@Json(name = "github_issues")
	val githubIssues: List<Int>,
	val devices: List<DeviceBase>,
	val firmware: Firmware?,
	val data: ProfileData,
) : ProfileBase(slug, name, subName, type, icon) {

	data class Firmware(
		val chip: String,
		val name: String,
		val version: String,
		val sdk: String,
		val key: String?,
	)
}
