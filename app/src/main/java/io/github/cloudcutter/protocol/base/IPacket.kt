/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.protocol.base

import io.github.cloudcutter.protocol.DGRAM_SIZE
import io.github.cloudcutter.protocol.OPT_LENGTH

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

    /**
     * Get offset of first options byte.
     */
    fun getOptionsOffset(): Int = DGRAM_SIZE - OPT_LENGTH - 4
}
