/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text

class PingWaitFoundAction(
	id: String,
	title: Text,
	nextId: String,
	timeout: Long = 10_000,
) : Action(id, title, nextId, timeout)
