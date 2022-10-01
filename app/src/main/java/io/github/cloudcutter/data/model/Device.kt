/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.model

class Device(
    slug: String,
    manufacturer: String,
    name: String,
    imageUrl: String?,

    val githubIssues: List<Int>,
    val imageUrls: List<String>,
    val profiles: List<ProfileBase>,
    val schemas: Map<String, List<Any>>,
) : DeviceBase(slug, manufacturer, name, imageUrl)
