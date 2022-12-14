/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-14.
 */

package io.github.cloudcutter.work.exceptions

import io.github.cloudcutter.R
import io.github.cloudcutter.util.Text

class PingException(
	val mode: Mode,
) : CloudcutterTextException(
	text = Text(when (mode) {
		Mode.EXPLOIT_TIMEOUT -> R.string.message_error_ping_found_timeout
		Mode.EXPLOIT_STILL_WORKING -> R.string.message_error_ping_lost_timeout
		Mode.GENERIC_TIMEOUT -> R.string.message_error_ping_found_timeout
	}),
) {

	enum class Mode {
		EXPLOIT_TIMEOUT,
		EXPLOIT_STILL_WORKING,
		GENERIC_TIMEOUT,
	}
}
