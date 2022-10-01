/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.model

import com.squareup.moshi.Json

open class DeviceBase(
	val slug: String,
	val manufacturer: String,
	val name: String,
	@Json(name = "image_url")
	val imageUrl: String?,
)
