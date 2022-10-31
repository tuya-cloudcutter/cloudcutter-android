/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.data.model

import com.mikepenz.iconics.typeface.IIcon
import com.squareup.moshi.Json

class ProfileLightleak(
	slug: String,
	name: String,
	@Json(name = "sub_name") subName: String?,
	type: Type,
	icon: IIcon?,
	@Json(name = "github_issues") githubIssues: List<Int>,
	devices: List<DeviceBase>,
	firmware: Firmware?,
	data: Data,
) : Profile<ProfileLightleak.Data>(
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

	open class Data(
		val type: Type,
		val bins: Binaries,
	) {

		enum class Type {
			BK7231T,
			BK7231N,
		}

		data class Binaries(
			val stager: String,
			val proper: String,
		)
	}
}
