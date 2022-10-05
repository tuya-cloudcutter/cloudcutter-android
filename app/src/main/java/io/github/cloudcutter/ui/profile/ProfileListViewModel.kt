/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.ui.profile

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cloudcutter.data.model.ProfileBase
import io.github.cloudcutter.data.repository.ProfileRepository
import io.github.cloudcutter.ui.base.DataViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileListViewModel @Inject constructor(
	private val profileRepository: ProfileRepository,
) : DataViewModel<List<ProfileBase>>() {

	override suspend fun loadDataImpl(): List<ProfileBase> {
		return profileRepository.getProfiles()
			.sortedBy { "${5 - it.type.ordinal} ${it.name} ${it.subName}" }
	}

	fun onProfileClicked(profile: ProfileBase) {
		navigate(ProfileListFragmentDirections.actionMenuProfilesToMenuWork(profile.slug))
	}
}
