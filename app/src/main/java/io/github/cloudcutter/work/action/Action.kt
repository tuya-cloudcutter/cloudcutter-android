/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text

abstract class Action(
	val id: String,
	val title: Text?,
	val nextId: String? = null,
	val timeout: Long? = null,
)
