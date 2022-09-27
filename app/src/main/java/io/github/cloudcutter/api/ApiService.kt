/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.api

import io.github.cloudcutter.data.IProfile
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

	@GET("profiles.json")
	fun getProfiles(): Call<List<IProfile>>
}
