/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.work.lightleak.command

data class CommandResponse<T : CommandRequest, D>(
	val command: T,
	val data: D,
)
