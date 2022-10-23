/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.work.service.lightleak.command

class FlashReadCommand(
	val offset: Int,
	val length: Int,
) : CommandRequest()
