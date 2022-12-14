/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-14.
 */

package io.github.cloudcutter.work.exceptions

import io.github.cloudcutter.R
import io.github.cloudcutter.util.Text

class PacketSendException : CloudcutterTextException(
	text = Text(R.string.message_error_packet_socket_exception),
)
