/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

package io.github.cloudcutter.work.event

import io.github.cloudcutter.util.MessageType
import io.github.cloudcutter.util.Text

class MessageEvent(
	val type: MessageType,
	val text: Text,
) : Event
