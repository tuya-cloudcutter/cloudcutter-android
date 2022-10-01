/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.model

import com.mikepenz.iconics.typeface.IIcon

class Profile(
    slug: String,
    name: String,
    type: Type,
    icon: IIcon?,

    val githubIssues: List<Int>,
    val devices: List<DeviceBase>,
    val firmware: Firmware?,
    val data: ProfileData,
) : ProfileBase(slug, name, type, icon) {

    data class Firmware(
        val chip: String,
        val name: String,
        val version: String,
        val sdk: String,
        val key: String?,
    )
}
