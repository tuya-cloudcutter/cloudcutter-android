/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.util

import com.squareup.moshi.Moshi
import io.github.cloudcutter.ext.get
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class CustomMoshiConverterFactory(
	private val moshi: Moshi,
) : Converter.Factory() {

	private val regex = """"0x([0-9A-Fa-f]+)"""".toRegex()

	override fun responseBodyConverter(
		type: Type,
		annotations: Array<out Annotation>,
		retrofit: Retrofit,
	) = Converter<ResponseBody, Any> { response ->
		var json = response.string()
		json = json.replace(regex) {
			'"' + it[1].toLong(16).toString() + '"'
		}
		return@Converter moshi.adapter<Any>(type).fromJson(json)
	}
}
