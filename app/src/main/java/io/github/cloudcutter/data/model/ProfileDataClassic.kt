/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.data.model

data class ProfileDataClassic(
	val addressFinish: Int,
	val addressSsid: Int?,
	val addressPasswd: Int?,
	val addressDatagram: Int?,
) : ProfileData
