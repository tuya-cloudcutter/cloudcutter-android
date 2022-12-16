/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.ui.base

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hadilq.liveevent.LiveEvent
import io.github.cloudcutter.util.FileLogger

abstract class BaseViewModel(logName: String? = null) : ViewModel() {

	protected val log = FileLogger(name = logName, className = this::class.java.simpleName)

	val navCommand = LiveEvent<NavDirections>()

	protected fun navigate(navDirections: NavDirections) {
		navCommand.value = navDirections
	}
}
