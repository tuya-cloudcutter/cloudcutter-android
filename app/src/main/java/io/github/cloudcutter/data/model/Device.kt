/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.model

import com.squareup.moshi.Json

class Device(
	slug: String,
	manufacturer: String,
	name: String,
	@Json(name = "image_url")
	imageUrl: String?,

	@Json(name = "github_issues")
	val githubIssues: List<Int>,
	@Json(name = "image_urls")
	val imageUrls: List<String>,
	val profiles: List<ProfileBase>,
	val schemas: Map<String, List<Any>>,
) : DeviceBase(slug, manufacturer, name, imageUrl)
