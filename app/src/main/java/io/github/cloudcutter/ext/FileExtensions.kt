/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-14.
 */

package io.github.cloudcutter.ext

import java.io.File

fun File.openChild(name: String) = File(this, name)

fun File.create(): File {
	if (!exists()) {
		parentFile?.mkdirs()
		createNewFile()
	}
	return this
}
