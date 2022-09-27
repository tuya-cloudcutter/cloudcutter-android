/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.data

data class ClassicProfile(
	override val key: String,
	override val name: String,
	val finishAddress: Int,
	val targetAddress: Int,
	val intermediateAddress: Int? = null,
	override val type: IProfile.Type = IProfile.Type.CLASSIC,
) : IProfile
