/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.R
import io.github.cloudcutter.util.Text
import kotlinx.coroutines.TimeoutCancellationException

class PingWaitFoundAction(
	id: String,
	title: Text,
	nextId: String,
	timeout: Long = 10_000,
) : Action(id, title, nextId, timeout, mapOf(
	TimeoutCancellationException::class.java to Text(R.string.message_error_ping_found_timeout),
))
