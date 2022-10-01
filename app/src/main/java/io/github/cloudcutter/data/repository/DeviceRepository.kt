/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.data.repository

import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.data.api.checkResponse
import io.github.cloudcutter.data.model.Device
import io.github.cloudcutter.data.model.DeviceBase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
	private val api: ApiService,
) {

	suspend fun getDevices(): List<DeviceBase> {
		return api.getDeviceList().checkResponse()
	}

	suspend fun getDevice(slug: String): Device {
		return api.getDevice(slug).checkResponse()
	}
}
