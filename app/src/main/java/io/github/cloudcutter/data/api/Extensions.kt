/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.api

import retrofit2.Response

@Throws(RuntimeException::class)
fun <T> Response<T>.toApiResponse(): ApiResponse<T> {
	val response = this

	val body = response.body()
	if (!response.isSuccessful || body == null) {
		throw RuntimeException(response.message())
	}

	return ApiResponse.success(body)
}
