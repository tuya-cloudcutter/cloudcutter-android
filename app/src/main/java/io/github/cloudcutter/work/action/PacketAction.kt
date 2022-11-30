/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.R
import io.github.cloudcutter.util.Text
import io.github.cloudcutter.work.protocol.base.IPacket
import java.net.SocketException

class PacketAction(
	id: String,
	title: Text,
	nextId: String?,
	val packet: IPacket,
) : Action(id, title, nextId, errorMap = mapOf(
	SocketException::class.java to Text(R.string.message_error_packet_socket_exception),
))
