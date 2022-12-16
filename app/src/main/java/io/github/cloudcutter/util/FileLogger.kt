/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-15.
 */

package io.github.cloudcutter.util

import android.util.Log
import io.github.cloudcutter.ext.create
import io.github.cloudcutter.ext.openChild
import io.github.cloudcutter.ext.toReadableString
import java.io.File
import java.time.LocalDateTime

class FileLogger(name: String?, className: String? = null) {

	private val logName = name ?: className
		?.removeSuffix("Fragment")
		?.removeSuffix("ViewModel")
		?.removeSuffix("Service")
	private var file: File? = null

	fun open(outputDir: File?) {
		file = outputDir?.openChild("log_${logName?.lowercase()}.txt")?.create()
	}

	operator fun invoke(vararg message: Any) {
		file ?: return
		val now = LocalDateTime.now().toReadableString()
		val caller = Thread.currentThread()
			.stackTrace
			.getOrNull(3)
			?.className
			?.substringAfterLast('.')
		val line = "[$now] [$caller] ${message.joinToString(" ")}"
		Log.d("Logger/$logName", line)
		file?.appendText("$line\n")
	}
}
