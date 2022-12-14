/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-14.
 */

package io.github.cloudcutter.ext

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

fun Int.roundTo(value: Int): Int = this.div(value).times(value)

fun Int.toReadableSize(unit: String = "iB", system: Double = 1024.0): String {
	if (this <= 0)
		return "-"
	val units = arrayOf("", "K", "M", "G", "T", "P")
	val digitGroups = (log10(this.toDouble()) / log10(system)).toInt()
	val value = DecimalFormat("#,##0.#")
		.format(this / system.pow(digitGroups.toDouble()))
	return "$value ${units[digitGroups]}$unit"
}
