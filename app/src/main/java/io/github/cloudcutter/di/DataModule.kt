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
import io.github.cloudcutter.data.model.ClassicProfile
import io.github.cloudcutter.data.model.FlashBasedProfile
import io.github.cloudcutter.data.model.IProfile
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

	@Singleton
	@Provides
	fun provideJsonFactory() = PolymorphicJsonAdapterFactory.of(IProfile::class.java, "type")
		.withSubtype(ClassicProfile::class.java, "CLASSIC")
		.withSubtype(FlashBasedProfile::class.java, "FLASH_BASED")

	@Singleton
	@Provides
	fun provideMoshi(factory: PolymorphicJsonAdapterFactory<IProfile>) = Moshi.Builder()
		.add(factory)
		.addLast(KotlinJsonAdapterFactory())
		.build()
}
