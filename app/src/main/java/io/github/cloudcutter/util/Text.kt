/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.util

import android.content.Context
import androidx.annotation.StringRes

class Text(
	@StringRes
	val res: Int,
	vararg val args: Any,
) {

	fun format(context: Context) = context.getString(res, *args)
}
