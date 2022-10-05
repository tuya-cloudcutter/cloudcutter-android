/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-4.
 */

package io.github.cloudcutter.work

import io.github.cloudcutter.work.action.Action

class ActionState(val action: Action) {
	var progress: Boolean = true
	var error: Throwable? = null
}
