/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.data

interface IProfile {
	val key: String
	val name: String
	val type: Type

	enum class Type {
		CLASSIC,
		FLASH_BASED,
	}
}
