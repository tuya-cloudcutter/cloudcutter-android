/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text
import io.github.cloudcutter.work.protocol.base.IPacket

class PacketAction(
	id: String,
	title: Text,
	nextId: String?,
	val packet: IPacket,
) : Action(id, title, nextId)
