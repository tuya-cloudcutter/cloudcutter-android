/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.di

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.util.CustomMoshiConverterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

	@Singleton
	@Provides
	fun provideOkHttpClient() = OkHttpClient.Builder()
		.addNetworkInterceptor(StethoInterceptor())
		.build()

	@Singleton
	@Provides
	fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi) = Retrofit.Builder()
		.baseUrl("http://192.168.0.120/")
		.client(okHttpClient)
		.addConverterFactory(CustomMoshiConverterFactory(moshi))
		.build()

	@Singleton
	@Provides
	fun provideApiService(retrofit: Retrofit) = retrofit.create(ApiService::class.java)
}
