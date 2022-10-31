/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-29.
 */

package io.github.cloudcutter.data.model

import com.squareup.moshi.Json

class ProfileLightleakDataT(
	bins: Binaries,
	@Json(name = "address_map") val addressMap: AddressMap,
	val gadgets: List<Gadget>,
) : ProfileLightleak.Data(Type.BK7231T, bins) {

	data class AddressMap(
		val magic: Int,
		val stager: Int,
		val buffer: Int,
		val store: Int,
		val intf: Int,
	)

	class Gadget(
		val name: String,
		@Json(name = "intf_offset") val intfOffset: Int?,
		val map: Map<Long, Int>,
	) {
		fun getStoreOffset(data: ProfileLightleakDataT) =
			intfOffset?.plus(data.addressMap.intf) ?: data.addressMap.store
	}

	fun getGadget(name: String) = gadgets.first { it.name == name }
}
