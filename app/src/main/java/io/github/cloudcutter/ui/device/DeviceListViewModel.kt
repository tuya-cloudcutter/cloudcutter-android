/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.ui.device

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cloudcutter.data.model.DeviceBase
import io.github.cloudcutter.data.repository.DeviceRepository
import io.github.cloudcutter.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class DeviceListViewModel @Inject constructor(
	private val deviceRepository: DeviceRepository,
) : BaseViewModel() {

	val devices = MutableLiveData<List<DeviceBase>>()
	val isLoading = MutableLiveData(true)

	suspend fun loadDevices() {
		val deviceList = deviceRepository.getDevices()
		devices.postValue(deviceList.sortedBy { it.slug })
		isLoading.postValue(false)
	}
}
