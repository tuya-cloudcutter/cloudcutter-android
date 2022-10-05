/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.event

abstract class Event {

	override fun toString(): String = this::class.java.simpleName
}
