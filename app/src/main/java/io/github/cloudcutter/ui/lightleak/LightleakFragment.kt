/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.ui.lightleak

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.core.content.getSystemService
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import io.github.cloudcutter.databinding.LightleakFragmentBinding
import io.github.cloudcutter.ext.toHexString
import io.github.cloudcutter.ui.base.BaseFragment
import io.github.cloudcutter.work.service.lightleak.LightleakService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress

@AndroidEntryPoint
class LightleakFragment : BaseFragment<LightleakFragmentBinding>({ inflater, parent ->
	LightleakFragmentBinding.inflate(inflater, parent, false)
}), CoroutineScope, ServiceConnection {
	companion object {
		private const val TAG = "LightleakFragment"
	}

	override val coroutineContext = Job() + Dispatchers.Main
	override val vm: LightleakViewModel by viewModels()
	private val args: LightleakFragmentArgs by navArgs()

	private val connectivityManager
		get() = context?.getSystemService<ConnectivityManager>()

	private val networkCallback = object : ConnectivityManager.NetworkCallback() {
		override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
			vm.binder?.setReturnIp(linkProperties.linkAddresses
				.map { it.address }
				.firstOrNull { it is Inet4Address }
				?: return
			)
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		b.vm = vm
		vm.progress.postValue(true)

		vm.result.observe(viewLifecycleOwner) { result ->
			val bytes = result.take(2).flatMap { it.toList() }
			val text = bytes.chunked(16).mapIndexed { index, chunk ->
				val offset = (index * 16).toString(16).padStart(6, '0')
				val part1 = chunk.subList(0, 8).toHexString()
				val part2 = chunk.subList(8, 16).toHexString()
				val ascii = chunk.map {
					if (it in 32..127)
						it.toInt().toChar()
					else
						'.'
				}.joinToString("")
				"$offset  $part1  $part2  |$ascii|"
			}
			b.hexView.text = text.joinToString("\n")
		}
	}

	override fun onStart() {
		super.onStart()
		Intent(requireContext(), LightleakService::class.java).also { intent ->
			context?.bindService(intent, this, Context.BIND_AUTO_CREATE)
		}
	}

	override fun onStop() {
		super.onStop()
		context?.unbindService(this)
		onServiceDisconnected(null)
	}

	override fun onServiceConnected(className: ComponentName, service: IBinder) {
		Log.d(TAG, "Service connected: $className")
		vm.binder = service as? LightleakService.ServiceBinder

		val networkRequest =
			NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
		connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
		connectivityManager?.requestNetwork(networkRequest, networkCallback)

		lifecycleScope.launch {
			withContext(Dispatchers.IO) {
				vm.prepare(args.profileSlug)
			}
		}
	}

	override fun onServiceDisconnected(className: ComponentName?) {
		Log.d(TAG, "Service disconnected: $className")
		vm.binder = null
		connectivityManager?.unregisterNetworkCallback(networkCallback)
	}
}
