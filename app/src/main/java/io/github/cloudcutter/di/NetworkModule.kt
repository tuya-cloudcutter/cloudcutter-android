/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.di

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.cloudcutter.Const.API_ENDPOINT
import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.ext.hasNetwork
import io.github.cloudcutter.util.CustomMoshiConverterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

	@Singleton
	@Provides
	fun provideOkHttpCache(context: Context): Cache =
		Cache(context.cacheDir, 5 * 1024 * 1024)

	@Singleton
	@Provides
	fun provideOkHttpClient(context: Context, cache: Cache): OkHttpClient =
		OkHttpClient.Builder()
			.cache(cache)
			.addInterceptor { chain ->
				val cacheControl =
					if (context.hasNetwork())
						"public, max-age=${12 * 60 * 60}"
					else
						"public, only-if-cached, max-stale=${7 * 24 * 60 * 60}"
				val request = chain.request()
					.newBuilder()
					.header("Cache-Control", cacheControl)
					.build()
				return@addInterceptor chain.proceed(request)
			}
			.addNetworkInterceptor(StethoInterceptor())
			.build()

	@Singleton
	@Provides
	fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
		Retrofit.Builder()
			.baseUrl(API_ENDPOINT)
			.client(okHttpClient)
			.addConverterFactory(CustomMoshiConverterFactory(moshi))
			.build()

	@Singleton
	@Provides
	fun provideApiService(retrofit: Retrofit): ApiService =
		retrofit.create(ApiService::class.java)
}
