/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.work.service.lightleak.command

import androidx.documentfile.provider.DocumentFile

class FlashReadCommand(
	val offset: Int,
	val length: Int,
	val output: DocumentFile,
) : CommandRequest()
