/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.ui.lightleak

import android.util.Log
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
import io.github.cloudcutter.work.lightleak.LightleakData
import io.github.cloudcutter.work.lightleak.LightleakService
import io.github.cloudcutter.work.lightleak.command.FlashReadCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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

	private var jobScope: CoroutineScope? = null
	val data = LightleakData()

	var binder: LightleakService.ServiceBinder? = null
		set(value) {
			field = value
			value?.setData(data)
		}

	fun start() {
		runCatching {
			jobScope?.cancel()
		}
		jobScope = CoroutineScope(Dispatchers.Main)
	}

	fun stop() {
		runCatching {
			jobScope?.cancel()
		}
		jobScope = null
	}

	suspend fun prepare(profileSlug: String): Profile<*> {
		if (data.profile.value != null)
			return data.profile.value!!
		data.progressRunning.postValue(true)
		val profile = profileRepository.getProfile(profileSlug) as? ProfileLightleak
			?: throw CloudcutterException("Couldn't load Lightleak profile")
		log("Profile: $profile")

		data.profile.postValue(profile)
		// write profile data to a file
		val profileJson = moshi.adapter<ProfileLightleak>(profile::class.java).toJson(profile)
		data.storageDir?.openChild("profile.json")?.create()?.writeText(profileJson)

		data.progressRunning.postValue(false)
		return profile
	}

	private fun runWithProgress(block: suspend () -> Unit) = jobScope?.launch {
		data.progressRunning.value = true
		try {
			block()
		} catch (e: Exception) {
			data.exception.value = e
		} finally {
			data.progressRunning.value = false
		}
	}

	fun onReadKeyblockClick() = flashRead(
		start = 0x200000 - 0x1000 + 0xE000 + 0x3000,
		length = 0x1000 + 0xE000 + 0x3000, // encrypted key + storage + swap
	)

	fun onReadFlashClick() =
		flashRead(0x000000, 0x200000)

	fun onReadFlashRangeClick(start: Int = 0x000000, length: Int = 0x200000) =
		flashRead(start, length)

	private fun flashRead(start: Int, length: Int) = runWithProgress {
		val outputDir = data.storageDir ?: return@runWithProgress
		val output = outputDir.openChild("dump_${outputDir.name}.bin").create()

		val response: List<ByteArray> =
			binder?.request(FlashReadCommand(start, length, output))
				?: return@runWithProgress

		Log.d(TAG, response.toString())
		data.result.value = response
	}
}
