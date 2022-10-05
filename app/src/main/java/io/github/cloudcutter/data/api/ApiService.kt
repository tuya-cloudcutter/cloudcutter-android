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

	@GET("api/devices.json")
	suspend fun getDeviceList(): Response<List<DeviceBase>>

	@GET("api/devices/{slug}.json")
	suspend fun getDevice(
		@Path("slug") slug: String,
	): Response<Device>

	@GET("api/profiles.json")
	suspend fun getProfileList(): Response<List<ProfileBase>>

	@GET("api/profiles/{slug}.json")
	suspend fun getProfile(
		@Path("slug") slug: String,
	): Response<Profile<*>>

	@GET("bins/{name}")
	suspend fun getBinary(
		@Path("name") name: String,
	): Response<ResponseBody>
}
