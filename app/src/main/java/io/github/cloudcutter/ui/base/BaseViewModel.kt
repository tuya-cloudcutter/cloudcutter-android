/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.ui.base

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import com.hadilq.liveevent.LiveEvent

abstract class BaseViewModel : ViewModel() {

	val navCommand = LiveEvent<NavDirections>()

	protected fun navigate(navDirections: NavDirections) {
		navCommand.value = navDirections
	}
}
