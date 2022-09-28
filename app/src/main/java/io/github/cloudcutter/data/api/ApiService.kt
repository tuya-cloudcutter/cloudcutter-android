/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.api

import io.github.cloudcutter.data.model.BasicProfile
import io.github.cloudcutter.data.model.IProfile
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

	@GET("profiles.json")
	suspend fun getProfileList(): Response<List<BasicProfile>>

	@GET("profiles/{key}.json")
	suspend fun getProfile(
		@Path("key") key: String,
	): Response<IProfile>
}
