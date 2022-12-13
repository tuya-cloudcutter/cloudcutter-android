/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-12.
 */

package io.github.cloudcutter.ui.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.getSystemService
import java.net.Inet4Address

interface NetworkAwareFragment {

	var networkAwareCallbacks: MutableList<Any?>?
	fun getContext(): Context?
	fun onAddressesChanged(local: Inet4Address?, gateway: Inet4Address?) {}
	fun onAddressesTextChanged(local: String?, gateway: String?) {}
	fun onConnectedSsidChanged(ssid: String?, rssi: Int?) {}

	val connectivityManager
		get() = getContext()?.getSystemService<ConnectivityManager>()
	val wifiManager
		get() = getContext()?.getSystemService<WifiManager>()

	private var networkCallback
		get() = networkAwareCallbacks?.get(0) as? NetworkCallback
		set(value) = networkAwareCallbacks?.set(0, value) as? Unit ?: Unit

	fun onStart() {
		if (networkAwareCallbacks == null) {
			networkAwareCallbacks = MutableList(2) { null }
			networkCallback = object : NetworkCallback() {
				override fun onLinkPropertiesChanged(
					network: Network,
					linkProperties: LinkProperties,
				) {
					this@NetworkAwareFragment.onLinkPropertiesChanged(linkProperties)
				}

				override fun onCapabilitiesChanged(
					network: Network,
					networkCapabilities: NetworkCapabilities,
				) {
					@Suppress("DEPRECATION")
					val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
						networkCapabilities.transportInfo as? WifiInfo
							?: wifiManager?.connectionInfo
					else
						wifiManager?.connectionInfo
					this@NetworkAwareFragment.onWifiInfoChanged(wifiInfo)
				}
			}
		}

		val networkRequest = NetworkRequest.Builder()
			.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
			.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
			.build()
		connectivityManager?.registerNetworkCallback(networkRequest, networkCallback ?: return)
		connectivityManager?.requestNetwork(networkRequest, networkCallback ?: return)
	}

	fun onStop() {
		connectivityManager?.unregisterNetworkCallback(networkCallback ?: return)
	}

	private fun onLinkPropertiesChanged(linkProperties: LinkProperties) {
		val localAddress = linkProperties.linkAddresses.firstOrNull { it.address is Inet4Address }
		val gatewayAddress =
			linkProperties.routes.firstOrNull { it.gateway is Inet4Address && it.destination.prefixLength == 0 }?.gateway
		onAddressesChanged(
			local = localAddress?.address as? Inet4Address,
			gateway = gatewayAddress as? Inet4Address,
		)
		onAddressesTextChanged(
			local = localAddress?.toString(),
			gateway = gatewayAddress?.hostAddress ?: gatewayAddress?.toString(),
		)
	}

	private fun onWifiInfoChanged(wifiInfo: WifiInfo?) {
		onConnectedSsidChanged(
			ssid = wifiInfo?.ssid?.drop(1)?.dropLast(1),
			rssi = wifiInfo?.rssi,
		)
	}
}
