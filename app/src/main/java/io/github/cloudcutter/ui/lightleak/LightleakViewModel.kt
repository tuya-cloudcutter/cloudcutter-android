/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.ui.lightleak

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.data.model.Profile
import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.data.repository.ProfileRepository
import io.github.cloudcutter.ui.base.BaseViewModel
import io.github.cloudcutter.work.service.lightleak.LightleakService
import io.github.cloudcutter.work.service.lightleak.command.KeyblockReadCommand
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

	suspend fun prepare(profileSlug: String) {
		if (isPrepared)
			return
		progress.postValue(true)
		val profile = profileRepository.getProfile(profileSlug) as? ProfileLightleak ?: return
		Log.d(TAG, "Profile: $profile")

		this.binder?.setProfile(profile.data)
		this.profile.postValue(profile)

		isPrepared = true
		progress.postValue(false)
	}

	fun onReadKeyblockClick() = viewModelScope.launch {
		progress.postValue(true)
		val response: List<ByteArray> = binder?.execute(KeyblockReadCommand()) ?: return@launch
		Log.d(TAG, response.toString())
		progress.postValue(false)
	}
}
