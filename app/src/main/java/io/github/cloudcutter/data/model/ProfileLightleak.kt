/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.data.model

import com.mikepenz.iconics.typeface.IIcon
import com.squareup.moshi.Json

class ProfileLightleak(
	slug: String,
	name: String,
	@Json(name = "sub_name") subName: String?,
	type: Type,
	icon: IIcon?,
	@Json(name = "github_issues") githubIssues: List<Int>,
	devices: List<DeviceBase>,
	firmware: Firmware?,
	data: Data,
) : Profile<ProfileLightleak.Data>(
	slug,
	name,
	subName,
	type,
	icon,
	githubIssues,
	devices,
	firmware,
	data,
) {

	data class Data(
		@Json(name = "address_map") val addressMap: AddressMap,
		val bins: Binaries,
		val gadgets: List<Gadget>,
	) {

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

		class Gadget(
			val name: String,
			@Json(name = "intf_offset") val intfOffset: Int?,
			val map: Map<Long, Int>,
		) {
			fun getStoreOffset(data: Data) =
				intfOffset?.plus(data.addressMap.intf) ?: data.addressMap.store
		}

		fun getGadget(name: String) = gadgets.first { it.name == name }
	}
}
