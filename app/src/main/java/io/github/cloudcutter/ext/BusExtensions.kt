/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-8.
 */

@file:Suppress("ControlFlowWithEmptyBody")

package io.github.cloudcutter.ext

import android.util.Log
import androidx.lifecycle.asFlow
import com.hadilq.liveevent.LiveEvent
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import io.github.cloudcutter.work.event.Event
import io.github.cloudcutter.work.service.lightleak.command.CommandRequest
import io.github.cloudcutter.work.service.lightleak.command.CommandResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

suspend inline fun <reified T : Event> Channel<Event>.await(): T {
	Log.d("Extensions", "Awaiting ${T::class.java.simpleName}")
	while (this.tryReceive().getOrNull() != null);
	while (true) {
		val event = receive()
		if (event is T) {
			Log.d("Extensions", "Event: $event")
			return event
		}
	}
}

suspend inline fun <reified T : Event> Channel<Event>.awaitTimeout(timeout: Long): T {
	Log.d("Extensions", "Awaiting ${T::class.java.simpleName} for $timeout ms")
	while (this.tryReceive().getOrNull() != null);
	while (true) {
		val event = withTimeout(timeout) { receive() }
		if (event is T) {
			Log.d("Extensions", "Event: $event")
			return event
		}
	}
}

suspend inline fun <reified T : Event> LiveEvent<Event>.await(): T {
	Log.d("Extensions", "Awaiting ${T::class.java.simpleName}")
	return this.asFlow().first { event ->
		if (event is T) {
			Log.d("Extensions", "Event: $event")
			return@first true
		}
		return@first false
	} as T
}

suspend inline fun <reified T : Event> LiveEvent<Event>.awaitTimeout(timeout: Long): T {
	return withTimeout(timeout) {
		await()
	}
}

suspend fun LiveEvent<Pair<Int, ByteArray>>.await(
	requestId: Int,
) = awaitCount(requestId, 1).first()

suspend fun LiveEvent<Pair<Int, ByteArray>>.awaitCount(
	requestId: Int,
	count: Int,
) = this.asFlow()
	.filter { it.first == requestId }
	.map { it.second }
	.take(count)
	.toList()

private class BusSubscriber<T : CommandRequest, D>(
	private val command: T,
	private val continuation: Continuation<CommandResponse<T, D>>,
) {
	companion object {
		private const val TAG = "BusSubscriber"
	}

	@Subscribe
	fun onValue(value: CommandResponse<T, D>) {
		Log.d(TAG, "Got value on bus: $value")
		if (value.command == command)
			continuation.resume(value)
	}
}

suspend fun <T : CommandRequest, D> Bus.awaitResponse(command: T): CommandResponse<T, D> {
	var subscriber: BusSubscriber<T, D>? = null
	val response = suspendCancellableCoroutine { continuation ->
		subscriber = BusSubscriber(command, continuation)
		continuation.invokeOnCancellation {
			this.unregister(subscriber)
		}
		this.register(subscriber)
	}
	this.unregister(subscriber)
	return response
}
