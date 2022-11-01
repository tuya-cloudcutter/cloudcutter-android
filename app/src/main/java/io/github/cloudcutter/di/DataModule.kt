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
import io.github.cloudcutter.data.model.ProfileLightleakDataN
import io.github.cloudcutter.data.model.ProfileLightleakDataT
import io.github.cloudcutter.util.MoshiIconAdapter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

	@Singleton
	@Provides
	fun provideMoshi(): Moshi =
		Moshi.Builder()
			.add(
				PolymorphicJsonAdapterFactory.of(Profile::class.java, "type")
					.withSubtype(
						ProfileClassic::class.java,
						ProfileBase.Type.CLASSIC.name,
					)
					.withSubtype(
						ProfileLightleak::class.java,
						ProfileBase.Type.LIGHTLEAK.name,
					)
			)
			.add(
				PolymorphicJsonAdapterFactory.of(ProfileLightleak.Data::class.java, "type")
					.withSubtype(
						ProfileLightleakDataT::class.java,
						ProfileLightleak.Data.Type.BK7231T.name,
					)
					.withSubtype(
						ProfileLightleakDataN::class.java,
						ProfileLightleak.Data.Type.BK7231N.name,
					)
			)
			.add(MoshiIconAdapter())
			.addLast(KotlinJsonAdapterFactory())
			.build()
}
