/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.ui.lightleak

import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.data.repository.ProfileRepository
import io.github.cloudcutter.ui.base.BaseViewModel
import io.github.cloudcutter.work.service.lightleak.LightleakService
import io.github.cloudcutter.work.service.lightleak.command.FlashReadCommand
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LightleakViewModel @Inject constructor(
	private val api: ApiService,
	private val profileRepository: ProfileRepository,
) : BaseViewModel() {
	companion object {
		private const val TAG = "LightleakViewModel"
	}

	private var isPrepared = false
	val progress = MutableLiveData<Boolean>()
	val profile = MutableLiveData<ProfileLightleak>()
	var binder: LightleakService.ServiceBinder? = null
		set(value) {
			field = value
			value?.setProfile(profile.value?.data ?: return)
		}

	lateinit var output: DocumentFile

	var result = MutableLiveData<List<ByteArray>>()

	suspend fun prepare(profileSlug: String) {
		if (isPrepared)
			return
		progress.postValue(true)
		val profile = profileRepository.getProfile(profileSlug) as? ProfileLightleak ?: return
		Log.d(TAG, "Profile: $profile")

		this.profile.postValue(profile)

		isPrepared = true
		progress.postValue(false)
	}

	fun onReadKeyblockClick() = viewModelScope.launch {
		val offset = 0x200000 - 0x3000 - 0xE000 - 0x1000
		val length = 0x1000 + 0xE000 // incl. encrypted key, excl. swap
		flashRead(offset, length)
	}

	fun onReadFlashClick() = viewModelScope.launch {
		flashRead(0x000000, 0x200000)
	}

	private suspend fun flashRead(start: Int, length: Int) {
		progress.postValue(true)
		val response: List<ByteArray> = binder?.execute(FlashReadCommand(start, length, output)) ?: return
		Log.d(TAG, response.toString())
		progress.postValue(false)
		result.postValue(response)
	}
}
