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
import io.github.cloudcutter.data.model.Profile
import io.github.cloudcutter.data.model.ProfileBase
import io.github.cloudcutter.data.model.ProfileClassic
import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.util.MoshiIconAdapter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

	@Singleton
	@Provides
	fun provideJsonFactory(): PolymorphicJsonAdapterFactory<Profile<*>> =
		PolymorphicJsonAdapterFactory.of(Profile::class.java, "type")
			.withSubtype(ProfileClassic::class.java, ProfileBase.Type.CLASSIC.name)
			.withSubtype(ProfileLightleak::class.java, ProfileBase.Type.LIGHTLEAK.name)

	@Singleton
	@Provides
	fun provideMoshi(factory: PolymorphicJsonAdapterFactory<Profile<*>>): Moshi =
		Moshi.Builder()
			.add(factory)
			.add(MoshiIconAdapter())
			.addLast(KotlinJsonAdapterFactory())
			.build()
}
