/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text

class PingWaitLostAction(
	id: String,
	title: Text,
	nextId: String,
	timeout: Long = 2_000,
) : Action(id, title, nextId, timeout)
