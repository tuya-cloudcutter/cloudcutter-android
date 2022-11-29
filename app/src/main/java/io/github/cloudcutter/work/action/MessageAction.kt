/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.MessageType
import io.github.cloudcutter.util.Text

class MessageAction(
	id: String,
	val type: MessageType,
	val text: Text,
	val autoClear: Boolean = true,
	nextId: String?,
) : Action(id, null, nextId)
