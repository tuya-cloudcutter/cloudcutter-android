/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.ext

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.getSystemService


operator fun MatchResult.get(i: Int) = this.groupValues[i]

fun Context.hasInternet(): Boolean {
	val cm = getSystemService<ConnectivityManager>()
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		return cm?.getNetworkCapabilities(cm.activeNetwork)
			?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
	} else {
		when (cm?.activeNetworkInfo?.type) {
			ConnectivityManager.TYPE_WIFI -> return true
			ConnectivityManager.TYPE_MOBILE -> return true
			ConnectivityManager.TYPE_VPN -> return true
		}
	}
	return false
}
