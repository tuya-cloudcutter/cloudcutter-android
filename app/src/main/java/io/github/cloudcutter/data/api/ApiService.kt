/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.data.api

import io.github.cloudcutter.data.model.Device
import io.github.cloudcutter.data.model.DeviceBase
import io.github.cloudcutter.data.model.Profile
import io.github.cloudcutter.data.model.ProfileBase
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

	@GET("devices.json")
	suspend fun getDeviceList(): Response<List<DeviceBase>>

	@GET("devices/{slug}.json")
	suspend fun getDevice(
		@Path("slug") slug: String,
	): Response<List<Device>>

	@GET("profiles.json")
	suspend fun getProfileList(): Response<List<ProfileBase>>

	@GET("profiles/{key}.json")
	suspend fun getProfile(
		@Path("slug") slug: String,
	): Response<Profile>

	@GET("bin/{name}")
	suspend fun getBinary(
		@Path("name") name: String,
	): Response<ResponseBody>
}
