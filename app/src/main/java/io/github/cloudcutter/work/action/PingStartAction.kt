/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text

class PingStartAction(
	id: String,
	title: Text?,
	nextId: String,
	val address: String,
) : Action(id, title, nextId)
