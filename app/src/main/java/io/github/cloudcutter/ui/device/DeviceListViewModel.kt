/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.ui.device

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cloudcutter.data.model.DeviceBase
import io.github.cloudcutter.data.repository.DeviceRepository
import io.github.cloudcutter.ui.base.DataViewModel
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
	private val deviceRepository: DeviceRepository,
) : DataViewModel<List<DeviceBase>>() {

	override suspend fun loadDataImpl(): List<DeviceBase> {
		return deviceRepository.getDevices().sortedBy { it.slug }
	}
}
