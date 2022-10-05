/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.data.repository

import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.data.api.checkResponse
import io.github.cloudcutter.data.model.Profile
import io.github.cloudcutter.data.model.ProfileBase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
	private val api: ApiService,
) {

	suspend fun getProfiles(): List<ProfileBase> {
		return api.getProfileList().checkResponse()
	}

	suspend fun getProfile(slug: String): Profile<*> {
		return api.getProfile(slug).checkResponse()
	}
}
