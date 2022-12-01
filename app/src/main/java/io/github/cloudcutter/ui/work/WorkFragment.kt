/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

package io.github.cloudcutter.ui.work

import android.animation.ObjectAnimator
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import dagger.hilt.android.AndroidEntryPoint
import io.github.cloudcutter.R
import io.github.cloudcutter.data.model.ProfileLightleak
import io.github.cloudcutter.databinding.WorkFragmentBinding
import io.github.cloudcutter.ext.openChild
import io.github.cloudcutter.ext.wifiConnect
import io.github.cloudcutter.ext.wifiScan
import io.github.cloudcutter.ui.base.BaseFragment
import io.github.cloudcutter.ui.input
import io.github.cloudcutter.work.action.WorkStateAction
import io.github.cloudcutter.work.event.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.receiveAsFlow
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class WorkFragment : BaseFragment<WorkFragmentBinding>({ inflater, parent ->
	WorkFragmentBinding.inflate(inflater, parent, false)
}), Observer<Event>, CoroutineScope {
	companion object {
		private const val TAG = "WorkFragment"
	}

	override val coroutineContext = Job() + Dispatchers.Main
	override val vm: WorkViewModel by viewModels()
	private val args: WorkFragmentArgs by navArgs()
	private var defaultIcon: IconicsDrawable? = null
	private var anim: ObjectAnimator? = null

	private val connectivityManager
		get() = context?.getSystemService<ConnectivityManager>()

	private val networkCallback = object : NetworkCallback() {
		override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
			vm.localAddress =
				linkProperties.linkAddresses.map { it.address }.firstOrNull { it is Inet4Address }
					?: return
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val adapter = WorkAdapter(vm.stateList)

		b.stateList.addItemDecoration(MaterialDividerItemDecoration(
			requireContext(),
			MaterialDividerItemDecoration.VERTICAL,
		).also { it.isLastItemDecorated = false })
		b.stateList.layoutManager = LinearLayoutManager(context)
		b.stateList.adapter = adapter

		vm.event.observe(viewLifecycleOwner, this)
		vm.stateAddedIndex.receiveAsFlow().asLiveData().observe(viewLifecycleOwner) {
			adapter.notifyItemInserted(it)
			b.stateList.smoothScrollToPosition(it)
			runProgressBar(it)
		}
		vm.stateChangedIndex.receiveAsFlow().asLiveData().observe(viewLifecycleOwner) {
			adapter.notifyItemChanged(it)
			runProgressBar(it)
		}

		lifecycleScope.launch {
			val profile = withContext(Dispatchers.IO) {
				vm.prepare(args.profileSlug)
			} ?: return@launch
			b.profile = profile
			// TODO move dialog functions when adding support for Classic, along with its config dialogs
			if (profile is ProfileLightleak) {
				chooseDirectoryInfoDialog()
			} else {
				startWork(null)
			}
		}
	}

	private fun chooseDirectoryInfoDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.work_directory_dialog_title)
			.setMessage(R.string.work_directory_dialog_text)
			.setPositiveButton(R.string.ok) { _, _ ->
				chooseDirectoryDialog()
			}
			.setNegativeButton(R.string.cancel) { _, _ ->
				navigateUp()
			}
			.setCancelable(false)
			.show()
	}

	private fun chooseDirectoryDialog() {
		val filesDir = requireContext().getExternalFilesDir(null) ?: requireContext().filesDir
		val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
		val deviceDirs = filesDir.listFiles { it: File ->
			it.isDirectory
		} ?: arrayOf<File>()
		// list all saved devices, current date and "add device" item
		val items = (deviceDirs as Array<File?>) + filesDir.openChild("${now}_lightleak") + null

		MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.work_directory_dialog_title)
			.setItems(items.map { directory ->
				directory?.name ?: requireContext().getString(R.string.work_directory_dialog_add)
			}.toTypedArray()) { dialog, which ->
				val item = items[which]
				if (item == null) {
					dialog.dismiss()
					chooseDirectoryNameDialog()
					return@setItems
				}
				vm.outputDir = item
				chooseStateInfoDialog()
			}
			.setNegativeButton(R.string.back) { _, _ ->
				chooseDirectoryInfoDialog()
			}
			.setCancelable(false)
			.show()
	}

	private fun chooseDirectoryNameDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.work_directory_name_dialog_title)
			.input(
				type = InputType.TYPE_CLASS_TEXT,
				positiveButton = R.string.ok,
				positiveListener = { _, input ->
					if (input.isEmpty())
						return@input false
					val name = input.lowercase()
						.trim()
						.replace(" ", "-")
						.replace("_", "-")
						.replace("[^a-z0-9-]".toRegex(), "")
						.replace("-+".toRegex(), "-")
						.trim('-')
					val filesDir = requireContext().getExternalFilesDir(null) ?: requireContext().filesDir
					filesDir.openChild(name).mkdirs()
					return@input true
				},
			)
			.setNegativeButton(R.string.cancel, null)
			.setOnDismissListener {
				chooseDirectoryDialog()
			}
			.setCancelable(false)
			.show()
	}

	private fun chooseStateInfoDialog() {
		MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.work_state_dialog_title)
			.setMessage(R.string.work_state_dialog_text)
			.setPositiveButton(R.string.ok) { _, _ ->
				chooseStateDialog()
			}
			.setNegativeButton(R.string.back) { _, _ ->
				chooseDirectoryDialog()
			}
			.setCancelable(false)
			.show()
	}

	private fun chooseStateDialog() {
		val items = listOf(
			R.string.work_state_dialog_raw,
			R.string.work_state_dialog_with_stager,
			R.string.work_state_dialog_running,
			R.string.work_state_dialog_connected,
		).map { requireContext().getString(it) }

		MaterialAlertDialogBuilder(requireContext())
			.setTitle(R.string.work_state_dialog_title)
			.setItems(items.toTypedArray()) { dialog, which ->
				dialog.dismiss()
				when (which) {
					0 -> startWork(null)
					1 -> startWork("message_device_connect_2")
					2 -> startWork("message_device_connect_3")
					3 -> startWork("work_state_running")
				}
			}
			.setNegativeButton(R.string.back) { _, _ ->
				chooseStateInfoDialog()
			}
			.setCancelable(false)
			.show()
	}

	private fun startWork(startActionId: String?) {
		lifecycleScope.launch {
			vm.run(startActionId)
		}
	}

	private fun runProgressBar(itemIndex: Int) {
		anim?.cancel()
		val state = vm.stateList[itemIndex]
		val action = state.action
		Log.d(TAG, "State changed progress=${state.progress} error=${state.error} $action")
		if (!state.progress) {
			b.messageProgress.progress = 0
			return
		}
		val timeout = action.timeout ?: return
		anim = ObjectAnimator.ofInt(
			b.messageProgress,
			"progress",
			timeout.toInt() / 10,
			0,
		)
		anim?.duration = timeout
		anim?.interpolator = LinearInterpolator()
		anim?.setAutoCancel(true)
		anim?.start()
	}

	override fun onChanged(value: Event) {
		launch {
			Log.d(TAG, "Event: $value")
			try {
				handleEvent(value)
			} catch (e: Exception) {
				lifecycleScope.cancel()
			}
		}
	}

	private suspend fun handleEvent(event: Event) {
		when (event) {
			is MessageEvent -> {
				b.messageCard.isVisible = true
				if (defaultIcon == null) defaultIcon = b.messageIcon.icon
				b.messageTitle.setText(event.type.title)
				b.messageText.text = event.text.format(context ?: return)
				b.messageIcon.icon = IconicsDrawable(context ?: return, event.type.icon).apply {
					sizeDp = 32
					colorRes = event.type.color
				}
			}
			is MessageRemoveEvent -> {
				b.messageCard.isVisible = false
				b.messageTitle.setText(R.string.message_title_running)
				b.messageText.setText(R.string.message_running)
				b.messageIcon.icon = defaultIcon
			}
			is WorkStateEvent -> {
				b.stateText.text = event.text.format(context ?: return)
			}
			is WiFiConnectRequest -> {
				var error: String?
				while (true) {
					error = context?.wifiConnect(event.ssid, event.password)
					if (error == null) break
				}
				vm.event.postValue(WiFiConnectResponse())
			}
			is WiFiScanRequest -> {
				val results = context?.wifiScan() ?: return
				delay(200)
				vm.event.postValue(results.toEvent())
			}
		}
	}

	override fun onStart() {
		super.onStart()
		val networkRequest =
			NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
				.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
		connectivityManager?.registerNetworkCallback(networkRequest, networkCallback)
		connectivityManager?.requestNetwork(networkRequest, networkCallback)
	}

	override fun onStop() {
		super.onStop()
		connectivityManager?.unregisterNetworkCallback(networkCallback)
	}
}
