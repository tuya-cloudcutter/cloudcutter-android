/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-23.
 */

package io.github.cloudcutter.ui.lightleak

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import io.github.cloudcutter.databinding.LightleakFragmentBinding
import io.github.cloudcutter.ext.launchWithErrorCard
import io.github.cloudcutter.ext.showError
import io.github.cloudcutter.ext.toHexString
import io.github.cloudcutter.ext.toReadableSize
import io.github.cloudcutter.ui.base.BaseFragment
import io.github.cloudcutter.ui.base.NetworkAwareFragment
import io.github.cloudcutter.work.lightleak.LightleakService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.File
import java.net.Inet4Address

@AndroidEntryPoint
class LightleakFragment : BaseFragment<LightleakFragmentBinding>({ inflater, parent ->
	LightleakFragmentBinding.inflate(inflater, parent, false)
}), CoroutineScope, ServiceConnection, NetworkAwareFragment {
	companion object {
		private const val TAG = "LightleakFragment"
		private const val PROGRESS_SPEED_SIZE = 50 // 50 KiB
	}

	override val coroutineContext = Job() + Dispatchers.Main
	override val vm: LightleakViewModel by viewModels()
	override var networkAwareCallbacks: MutableList<Any?>? = null

	private val args: LightleakFragmentArgs by navArgs()
	private val data = vm.data
	private var progressLast = 0
	private var progressLastAt = 0L
	private var progressSpeedList = listOf<Int>()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		b.vm = vm
		data.progressRunning.value = true

		requirePermissions(
			android.Manifest.permission.ACCESS_COARSE_LOCATION,
			android.Manifest.permission.ACCESS_FINE_LOCATION,
			android.Manifest.permission.ACCESS_NETWORK_STATE,
			android.Manifest.permission.ACCESS_WIFI_STATE,
			android.Manifest.permission.CHANGE_WIFI_STATE,
			android.Manifest.permission.NEARBY_WIFI_DEVICES
				.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU },
		)
	}

	override suspend fun onPermissionsGranted() {
		if (!isAdded) return

		data.storageDir = File(args.storageDir)
		log.open(data.storageDir)
		vm.log.open(data.storageDir)
		data.serviceLog.open(data.storageDir)
		b.outputDirectory.text = data.storageDir?.absolutePath

		launchWithErrorCard(b.messageCard) {
			val profile = withContext(Dispatchers.IO) {
				vm.prepare(args.profileSlug)
			}
			b.profileInfo.profile = profile
		}

		data.exception.observe(viewLifecycleOwner) {
			showError(b.messageCard, it)
		}

		data.progressBytes.observe(viewLifecycleOwner) { progress ->
			if (progress == null) {
				b.readSpeed.text = null
				b.progressText.text = null
				return@observe
			}
			val now = System.currentTimeMillis()
			if (progressLast != 0) {
				val readBytes = progress - progressLast
				val readTime = now - progressLastAt
				val readSpeed = ((1000f / readTime) * readBytes).toInt()
				val percentage = data.progressValue.value ?: 0
				b.progressText.text = "$percentage% - " + progress.toReadableSize()
				if (readSpeed != 0) {
					progressSpeedList = progressSpeedList.takeLast(PROGRESS_SPEED_SIZE - 1) + readSpeed
					val speedAverage = progressSpeedList.average().toInt()
					b.readSpeed.text = speedAverage.toReadableSize("iB/s")
				}
			}
			progressLast = progress
			progressLastAt = now
		}

		data.result.observe(viewLifecycleOwner) { result ->
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

	override fun onAddressesChanged(local: Inet4Address?, gateway: Inet4Address?) {
		data.localAddress = local
		data.gatewayAddress = gateway
	}

	override fun onAddressesTextChanged(local: String?, gateway: String?) {
		b.wifiState.localAddress = local
		b.wifiState.gatewayAddress = gateway
		log("IP addresses changed: $local / $gateway")
	}

	override fun onConnectedSsidChanged(ssid: String?, rssi: Int?) {
		b.wifiState.wifiSsid = ssid
		b.wifiState.wifiRssi = rssi ?: 0
		log("Wi-Fi SSID changed: $ssid")
	}

	override fun onStart() {
		super<BaseFragment>.onStart()
		super<NetworkAwareFragment>.onStart()
		vm.start()
		Intent(requireContext(), LightleakService::class.java).also { intent ->
			context?.bindService(intent, this, Context.BIND_AUTO_CREATE)
		}
	}

	override fun onStop() {
		super<BaseFragment>.onStop()
		super<NetworkAwareFragment>.onStop()
		vm.stop()
		context?.unbindService(this)
		onServiceDisconnected(null)
	}

	override fun onServiceConnected(className: ComponentName, service: IBinder) {
		log("Service connected: $className")
		vm.binder = service as? LightleakService.ServiceBinder
	}

	override fun onServiceDisconnected(className: ComponentName?) {
		log("Service disconnected: $className")
		vm.binder = null
	}
}
