/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.R
import io.github.cloudcutter.util.Text
import kotlinx.coroutines.TimeoutCancellationException

class PingAction(
	id: String,
	title: Text,
	nextId: String,
	timeout: Long = 5_000,
	val mode: Mode,
	val address: String,
	val threshold: Int,
) : Action(id, title, nextId, timeout, mapOf(
	TimeoutCancellationException::class.java to Text(when (mode) {
		Mode.FOUND -> R.string.message_error_ping_found_timeout
		Mode.LOST -> R.string.message_error_ping_lost_timeout
	}),
)) {

	enum class Mode {
		FOUND,
		LOST,
	}
}
