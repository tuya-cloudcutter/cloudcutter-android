/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.model

import com.mikepenz.iconics.typeface.IIcon
import com.squareup.moshi.Json

open class ProfileBase(
	val slug: String,
	val name: String,
	@Json(name = "sub_name")
	val subName: String?,
	val type: Type,
	val icon: IIcon?,
) {

	enum class Type {
		CLASSIC,
		LIGHTLEAK,
	}
}
