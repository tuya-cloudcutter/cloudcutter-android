/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.model

import com.mikepenz.iconics.typeface.IIcon

open class ProfileBase(
	val slug: String,
	val name: String,
	val type: Type,
	val icon: IIcon?,
) {
	enum class Type {
		CLASSIC,
		LIGHTLEAK,
	}
}
