/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.model

import com.mikepenz.iconics.typeface.IIcon

data class BasicProfile(
	override val key: String,
	override val name: String,
	override val type: IProfile.Type,
	val icon: IIcon,
) : IProfile
