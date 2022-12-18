/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-18.
 */

package io.github.cloudcutter.work.lightleak

import androidx.lifecycle.MutableLiveData
import io.github.cloudcutter.data.model.ProfileLightleak
import java.io.File
import java.net.Inet4Address

class LightleakData {

	val profile = MutableLiveData<ProfileLightleak>()
	val exception = MutableLiveData<Exception>()
	val progressRunning = MutableLiveData<Boolean>()
	val progressValue = MutableLiveData<Int?>()
	val progressBytes = MutableLiveData<Int?>()

	var storageDir: File? = null
	var localAddress: Inet4Address? = null
	var gatewayAddress: Inet4Address? = null

	val profileData
		get() = profile.value?.data

	// TODO get rid of this
	var result = MutableLiveData<List<ByteArray>>()
}
