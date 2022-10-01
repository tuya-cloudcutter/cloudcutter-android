/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.ext

import android.content.Context
import android.net.ConnectivityManager

operator fun MatchResult.get(i: Int) = this.groupValues[i]

fun Context.hasNetwork(): Boolean {
	val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
	return connectivityManager.activeNetworkInfo?.isConnected ?: false
}
