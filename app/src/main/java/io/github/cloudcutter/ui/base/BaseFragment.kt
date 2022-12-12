/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.ui.base

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.cloudcutter.R
import io.github.cloudcutter.ui.main.MainViewModel

abstract class BaseFragment<B : ViewBinding>(
	private val inflater: (inflater: LayoutInflater, parent: ViewGroup?) -> B,
) : Fragment() {

	protected lateinit var b: B
	protected abstract val vm: BaseViewModel
	protected val activity: MainViewModel by activityViewModels()

	private lateinit var permissionLauncher: ActivityResultLauncher<String>
	private var neededPermissions: List<String>? = null
	private var askedPermission: String? = null

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? {
		b = this.inflater(inflater, container)
		if (b is ViewDataBinding)
			(b as ViewDataBinding).lifecycleOwner = viewLifecycleOwner
		return b.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		vm.navCommand.observe(viewLifecycleOwner) {
			findNavController().navigate(it)
		}
		permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
			val permission = askedPermission ?: return@registerForActivityResult
			if (isGranted) {
				// ask for remaining permissions
				neededPermissions?.filterNot { it == permission }?.toTypedArray()?.let {
					requirePermissions(*it)
				}
				return@registerForActivityResult
			}

			if (shouldShowRequestPermissionRationale(permission)) {
				// denied temporarily, just ask again
				neededPermissions?.toTypedArray()?.let {
					requirePermissions(*it)
				}
				return@registerForActivityResult
			}

			// permanently denied - show a dialog with explanation
			MaterialAlertDialogBuilder(requireContext())
				.setTitle(R.string.permission_denied_title)
				.setMessage(R.string.permission_denied_text)
				.setPositiveButton(R.string.ok, null)
				.setOnDismissListener {
					navigateUp()
				}
				.show()
		}
	}

	protected fun navigateUp() {
		findNavController().navigateUp()
	}

	protected open fun onPermissionsGranted() {}

	protected fun requirePermissions(vararg permissions: String?) {
		neededPermissions = permissions.filterNotNull().filter {
			ContextCompat.checkSelfPermission(
				requireContext(),
				it,
			) == PackageManager.PERMISSION_DENIED
		}
		if (neededPermissions?.isEmpty() != false) {
			onPermissionsGranted()
			return
		}
		askedPermission = neededPermissions?.firstOrNull() ?: return
		permissionLauncher.launch(askedPermission)
	}
}
