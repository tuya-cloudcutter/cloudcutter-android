/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

package io.github.cloudcutter.work.event

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout

suspend inline fun <reified T : Event> Channel<Event>.await(): T {
	while (true) {
		val event = receive()
		if (event is T) return event
	}
}

suspend inline fun <reified T : Event> Channel<Event>.awaitTimeout(timeout: Long): T? {
	while (true) {
		try {
			val event = withTimeout(timeout) { receive() }
			if (event is T) return event
		} catch (e: TimeoutCancellationException) {
			return null
		}
	}
}
