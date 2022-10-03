/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.util

import androidx.annotation.StringRes

class Text(
	@StringRes
	val res: Int,
	vararg val args: Any,
)
