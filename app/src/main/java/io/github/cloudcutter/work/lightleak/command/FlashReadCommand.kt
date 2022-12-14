/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.work.lightleak.command

import java.io.File

class FlashReadCommand(
	val offset: Int,
	val length: Int,
	val output: File,
) : CommandRequest()
