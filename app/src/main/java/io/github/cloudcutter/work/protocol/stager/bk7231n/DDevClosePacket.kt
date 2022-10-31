/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-31.
 */

package io.github.cloudcutter.work.protocol.stager.bk7231n

import io.github.cloudcutter.data.model.ProfileLightleakDataN

data class DDevClosePacket(
	private val profile: ProfileLightleakDataN,
) : StagerPacket(profile) {

	override val target = profile.getGadget("ddev_close")
}
