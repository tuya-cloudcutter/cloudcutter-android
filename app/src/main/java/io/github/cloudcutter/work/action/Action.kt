/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.R
import io.github.cloudcutter.util.CloudcutterException
import io.github.cloudcutter.util.Text
import kotlinx.coroutines.TimeoutCancellationException

abstract class Action(
	val id: String,
	val title: Text?,
	val nextId: String? = null,
	val timeout: Long? = null,
	val errorMap: Map<Class<*>, Text> = mapOf(),
) {

	fun getErrorText(e: Throwable): Text {
		for ((cls, text) in errorMap) {
			if (e::class.java == cls) return text
		}

		return when (e) {
			is TimeoutCancellationException -> Text(R.string.message_error_timeout)
			is CloudcutterException -> Text(R.string.text_format_string, e.message!!)
			else -> Text(R.string.message_error_unknown, e.toString())
		}
	}

	override fun toString(): String {
		return "${this::class.java.simpleName}(${id})"
	}
}
