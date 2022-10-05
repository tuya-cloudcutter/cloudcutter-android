/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

package io.github.cloudcutter.util

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import io.github.cloudcutter.R

enum class MessageType(
	@StringRes val title: Int,
	val icon: IIcon,
	@ColorRes val color: Int,
) {
	ERROR(
		title = R.string.message_title_error,
		icon = CommunityMaterial.Icon.cmd_alert_circle_outline,
		color = R.color.icon_error,
	),
	WARNING(
		title = R.string.message_type_warning,
		icon = CommunityMaterial.Icon.cmd_alert_outline,
		color = R.color.icon_warning,
	),
	INFO(
		title = R.string.message_type_info,
		icon = CommunityMaterial.Icon2.cmd_information_outline,
		color = R.color.icon_info,
	),
	SUCCESS(
		title = R.string.message_type_success,
		icon = CommunityMaterial.Icon.cmd_check_circle_outline,
		color = R.color.icon_success,
	),
}
