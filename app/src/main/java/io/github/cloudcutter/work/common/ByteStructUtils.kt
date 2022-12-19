/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-19.
 */

package io.github.cloudcutter.work.common

import kotlin.reflect.KClass

annotation class ByteStructFormat(
	val format: String,
)

annotation class ByteStructChild(
	val typeFieldIndex: Int,
	val typeFieldValue: Int,
	val dataFieldIndex: Int,
	val dataFieldClass: KClass<out ByteStruct>,
	val mode: Int = 0,
)

annotation class ByteStructChildMap(
	vararg val map: ByteStructChild,
)
