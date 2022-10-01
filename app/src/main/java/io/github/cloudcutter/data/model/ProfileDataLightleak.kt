/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.data.model

import com.squareup.moshi.Json

data class ProfileDataLightleak(
	@Json(name = "address_map")
	val addressMap: AddressMap,
	val bins: Binaries,
	val gadgets: List<Gadget>,
) : ProfileData {

	data class AddressMap(
		val magic: Int,
		val stager: Int,
		val buffer: Int,
		val store: Int,
		val intf: Int,
	)

	data class Binaries(
		val stager: String,
		val proper: String,
	)

	inner class Gadget(
		val name: String,
		@Json(name = "intf_offset")
		val intfOffset: Int?,
		val map: Map<Long, Int>,
	) {
		fun getStoreOffset() = intfOffset?.plus(addressMap.intf) ?: addressMap.store
	}

	fun getGadget(name: String) = gadgets.first { it.name == name }
}
