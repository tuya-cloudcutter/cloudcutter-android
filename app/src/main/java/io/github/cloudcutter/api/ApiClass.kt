/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.github.cloudcutter.data.ClassicProfile
import io.github.cloudcutter.data.FlashBasedProfile
import io.github.cloudcutter.data.IProfile
import retrofit2.Retrofit

class ApiClass {

	private val service: ApiService

	init {
		val moshi = Moshi.Builder()
			.add(PolymorphicJsonAdapterFactory.of(IProfile::class.java, "type")
				.withSubtype(ClassicProfile::class.java, "CLASSIC")
				.withSubtype(FlashBasedProfile::class.java, "FLASH_BASED"))
			.addLast(KotlinJsonAdapterFactory())
			.build()

		val retrofit = Retrofit.Builder()
			.baseUrl("http://example.com/")
			.addConverterFactory(CustomMoshiConverterFactory(moshi))
			.build()

		service = retrofit.create(ApiService::class.java)
	}

	fun getProfiles(): List<IProfile> {
		val response = service.getProfiles()
			.execute()
		val body = response.body()
		if (!response.isSuccessful || body == null) {
			throw RuntimeException("Request failed")
		}
		return body
	}
}
