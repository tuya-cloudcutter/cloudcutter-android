/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter

import android.app.Application
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.facebook.stetho.Stetho
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

	override fun onCreate() {
		super.onCreate()
		Stetho.initializeWithDefaults(this)

		CaocConfig.Builder.create()
			.backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
			.enabled(true)
			.showErrorDetails(true)
			.showRestartButton(true)
			.logErrorOnRestart(true)
			.trackActivities(false)
			.minTimeBetweenCrashesMs(10000)
			.errorDrawable(R.mipmap.ic_launcher_round)
			.restartActivity(null)
			.errorActivity(null)
			.eventListener(null)
			.customCrashDataCollector(null)
			.apply()

	}
}
