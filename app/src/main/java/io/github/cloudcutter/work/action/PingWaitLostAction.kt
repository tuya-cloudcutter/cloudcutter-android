/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.R
import io.github.cloudcutter.util.Text
import kotlinx.coroutines.TimeoutCancellationException

class PingWaitLostAction(
	id: String,
	title: Text,
	nextId: String,
	timeout: Long = 2_000,
) : Action(id, title, nextId, timeout, mapOf(
	TimeoutCancellationException::class.java to Text(R.string.message_error_ping_lost_timeout),
))
