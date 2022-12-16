/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.ui.lightleak

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.data.model.Profile
import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.data.repository.ProfileRepository
import io.github.cloudcutter.ext.create
import io.github.cloudcutter.ext.openChild
import io.github.cloudcutter.ui.base.BaseViewModel
import io.github.cloudcutter.work.exceptions.CloudcutterException
import io.github.cloudcutter.work.lightleak.LightleakService
import io.github.cloudcutter.work.lightleak.command.FlashReadCommand
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.net.Inet4Address
import javax.inject.Inject

@HiltViewModel
class LightleakViewModel @Inject constructor(
	private val api: ApiService,
	private val profileRepository: ProfileRepository,
	private val moshi: Moshi,
) : BaseViewModel() {
	companion object {
		private const val TAG = "LightleakViewModel"
	}

	private var isPrepared = false
	private var progressJob: Job? = null

	var storageDir: File? = null
		set(value) {
			field = value
			log.open(field ?: return)
		}

	val profile = MutableLiveData<ProfileLightleak>()
	val progressRunning = MutableLiveData<Boolean>()
	val progressValue = MutableLiveData<Int?>()
	val progressBytes = MutableLiveData<Int?>()
	var result = MutableLiveData<List<ByteArray>>()

	var localAddress: Inet4Address? = null
		set(value) {
			field = value
			binder?.localAddress = value
		}
	var gatewayAddress: Inet4Address? = null
		set(value) {
			field = value
			binder?.gatewayAddress = value
		}

	var binder: LightleakService.ServiceBinder? = null
		set(value) {
			field = value
			value?.setData(
				profile = profile.value?.data,
				progressValue = progressValue,
				progressBytes = progressBytes,
			)
			value?.localAddress = localAddress
			value?.gatewayAddress = gatewayAddress
		}

	suspend fun prepare(profileSlug: String): Profile<*> {
		if (isPrepared && this.profile.value != null)
			return this.profile.value!!
		progressRunning.postValue(true)
		val profile = profileRepository.getProfile(profileSlug) as? ProfileLightleak
			?: throw CloudcutterException("Couldn't load Lightleak profile")
		Log.d(TAG, "Profile: $profile")

		this.profile.postValue(profile)
		// force setting the profile
		binder?.setData(
			profile = profile.data,
			progressValue = progressValue,
			progressBytes = progressBytes,
		)
		// write profile data to a file
		val profileJson = moshi.adapter<ProfileLightleak>(profile::class.java).toJson(profile)
		storageDir?.openChild("profile.json")?.create()?.writeText(profileJson)

		isPrepared = true
		progressRunning.postValue(false)
		return profile
	}

	fun cancel() {
		progressJob?.cancel()
	}

	fun onReadKeyblockClick() {
		progressJob = viewModelScope.launch {
			val length = 0x1000 + 0xE000 + 0x3000 // encrypted key + storage + swap
			val offset = 0x200000 - length
			flashRead(offset, length)
		}
	}

	fun onReadFlashClick() {
		progressJob = viewModelScope.launch {
			flashRead(0x000000, 0x200000)
		}
	}

	fun onReadFlashRangeClick(start: Int = 0x000000, length: Int = 0x200000) {
		progressJob = viewModelScope.launch {
			flashRead(start, length)
		}
	}

	private suspend fun flashRead(start: Int, length: Int) {
		// TODO handle exceptions here
		progressRunning.postValue(true)
		val outputDir = storageDir ?: return
		val output = outputDir.openChild("dump_${outputDir.name}.bin").create()
		val response: List<ByteArray> =
			binder?.execute(FlashReadCommand(start, length, output)) ?: return
		Log.d(TAG, response.toString())
		progressRunning.postValue(false)
		result.postValue(response)
		progressJob = null
	}
}
