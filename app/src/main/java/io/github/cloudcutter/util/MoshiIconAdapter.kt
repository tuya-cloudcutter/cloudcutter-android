/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.util

import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class MoshiIconAdapter {

	@ToJson
	fun toJson(value: IIcon): String {
		return value.name
	}

	@FromJson
	fun fromJson(value: String): IIcon {
		return CommunityMaterial.getIcon("cmd_${value.replace("-", "_")}")
	}
}
