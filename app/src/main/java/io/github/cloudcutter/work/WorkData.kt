/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work

import io.github.cloudcutter.data.model.Profile

class WorkData(
	val profile: Profile,
) {

	val actionTimeout = 10 * 60 * 1000L

	val idleSsid = "LightleakIdle"
	val idlePassword = "cl0udcutt3r!@#"
	val idleAddress = "10.0.0.1"
	val idlePort = 8080
	val targetSsidPrefix = "CCTR-"
	val targetSsidRegex = """^[\w\d +-]+-[A-F0-9]{4}$""".toRegex()
	val targetAddress = "192.168.175.1"
	val targetBroadcast = "255.255.255.255"

	var lightleakSsid = "LightleakCustom".toByteArray()
	lateinit var lightleakPassword: ByteArray
	lateinit var lightleakProper: ByteArray

	lateinit var newUuid: String
	lateinit var newAuthKey: String
}
