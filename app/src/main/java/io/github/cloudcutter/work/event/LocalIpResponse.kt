/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

package io.github.cloudcutter.work.event

import java.net.InetAddress

class LocalIpResponse(
	val address: InetAddress,
) : Event
