/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-31.
 */

package io.github.cloudcutter.data.model

import com.squareup.moshi.Json

class ProfileLightleakDataN(
	bins: Binaries,
	@Json(name = "address_map") val addressMap: AddressMap,
	val gadgets: List<Gadget>,
) : ProfileLightleak.Data(Type.BK7231T, bins) {

	data class AddressMap(
		val stager: Int,
		val buffer: Int,
		val handle: Int,
		val temp: Int,
		val intf: Int,
	)

	class Gadget(
		val name: String,
		@Json(name = "intf_offset") val intfOffset: Int?,
		val address: Int,
		@Json(name = "branch_offset") val branchOffset: Int,
	)

	fun getGadget(name: String) = gadgets.first { it.name == name }
}
