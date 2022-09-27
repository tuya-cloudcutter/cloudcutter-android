/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-26.
 */

package io.github.cloudcutter.protocol

const val DGRAM_SIZE = 256
const val DGRAM_HEAD = 0x55AA
const val DGRAM_TAIL = 0xAA55
const val DGRAM_FR_NUM = 0x00
const val DGRAM_FR_TYPE = 0x01

const val OPT_LENGTH = 0x0C
const val OPT_MARKER = 0xFF

const val CMD_FINISH = 0x02
const val CMD_RUN_INT = 0x00
const val CMD_RUN_PTR = 0x01
