/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.work.service.lightleak.command

abstract class CommandRequest(
	val commandId: Long = System.currentTimeMillis(),
)
