/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.work.protocol.base

interface IPacket {

	/**
	 * Return binary values to store in the packet's JSON structure.
	 */
	fun getJsonFields(): Map<String, ByteArray>

	/**
	 * Return command to store in the datagram.
	 */
	fun getCommand(): ByteArray?

	/**
	 * Return other parameters to store in the datagram.
	 */
	fun getOptions(): ByteArray?

	/**
	 * Serialize the entire datagram.
	 */
	fun serialize(): ByteArray

	/**
	 * Get offset of first command byte.
	 */
	fun getCommandOffset(): Int = 0x48
}
