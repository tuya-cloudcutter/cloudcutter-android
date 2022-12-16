/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-16.
 */

package io.github.cloudcutter.util

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class RequestBus<T> {
	companion object {
		private const val TAG = "RequestBus"
	}

	private class Response<T, Any>(
		val request: T,
		val result: Result<Any>,
	)

	private val requests = Channel<T>(Channel.UNLIMITED)
	private val responses = Channel<Response<T, Any>>(Channel.UNLIMITED)

	fun subscribe(
		coroutineScope: CoroutineScope,
		handler: suspend (request: T) -> Any,
	) = coroutineScope.launch {
		Log.d(TAG, "Subscribing bus")
		requests.consumeEach { request ->
			Log.d(TAG, "Got request: $request")
			val result = runCatching {
				handler(request)
			}
			Log.d(TAG, "Handler returned $result")
			responses.send(Response(request, result))
		}
	}

	@Throws(Exception::class)
	suspend fun <D> request(request: T): D {
		Log.d(TAG, "Requesting $request")
		requests.send(request)
		while (true) {
			val response = responses.receive()
			if (response.request != request)
				continue
			Log.d(TAG, "Got matching result: ${response.result}")
			return response.result.getOrThrow() as D
		}
	}
}
