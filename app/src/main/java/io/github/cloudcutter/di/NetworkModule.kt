/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.di

import android.content.Context
import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.cloudcutter.Const.API_ENDPOINT
import io.github.cloudcutter.data.api.ApiService
import io.github.cloudcutter.ext.hasInternet
import io.github.cloudcutter.util.CustomMoshiConverterFactory
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
	companion object {
		private const val TAG = "NetworkModule"
	}

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
				val cacheControlList = if (context.hasInternet())
					listOf(CacheControl.FORCE_NETWORK, CacheControl.FORCE_CACHE)
				else
					listOf(CacheControl.FORCE_CACHE)

				var error: Throwable? = null
				for (cacheControl in cacheControlList) {
					try {
						Log.d(TAG, "Cache control: $cacheControl")
						val request = chain.request()
							.newBuilder()
							.cacheControl(cacheControl)
							.build()
						return@addInterceptor chain.proceed(request)
					} catch (e: Exception) {
						error = error ?: e
						Log.e(TAG, "Failed: $e")
					}
				}
				throw error!!
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
