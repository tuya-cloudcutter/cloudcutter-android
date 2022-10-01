/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.cloudcutter.data.model.ProfileBase
import io.github.cloudcutter.data.model.ProfileData
import io.github.cloudcutter.data.model.ProfileDataClassic
import io.github.cloudcutter.data.model.ProfileDataLightleak
import io.github.cloudcutter.util.MoshiIconAdapter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

	@Singleton
	@Provides
	fun provideJsonFactory(): PolymorphicJsonAdapterFactory<ProfileData> =
		PolymorphicJsonAdapterFactory.of(ProfileData::class.java, "type")
			.withSubtype(ProfileDataClassic::class.java, ProfileBase.Type.CLASSIC.name)
			.withSubtype(ProfileDataLightleak::class.java, ProfileBase.Type.LIGHTLEAK.name)

	@Singleton
	@Provides
	fun provideMoshi(factory: PolymorphicJsonAdapterFactory<ProfileData>): Moshi =
		Moshi.Builder()
			.add(factory)
			.add(MoshiIconAdapter())
			.addLast(KotlinJsonAdapterFactory())
			.build()
}
