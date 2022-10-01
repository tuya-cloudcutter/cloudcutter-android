/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.ui.device

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import io.github.cloudcutter.databinding.DeviceListFragmentBinding
import io.github.cloudcutter.ui.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class DeviceListFragment : BaseFragment<DeviceListFragmentBinding>({ inflater, parent ->
	DeviceListFragmentBinding.inflate(inflater, parent, false)
}) {

	override val vm: DeviceListViewModel by viewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		b.vm = vm

		b.list.addItemDecoration(MaterialDividerItemDecoration(
			requireContext(),
			MaterialDividerItemDecoration.VERTICAL,
		))
		b.list.addItemDecoration(MaterialDividerItemDecoration(
			requireContext(),
			MaterialDividerItemDecoration.HORIZONTAL,
		))
		b.list.layoutManager = GridLayoutManager(context, 3)

		vm.devices.observe(viewLifecycleOwner) {
			b.list.adapter = DeviceListAdapter(it)
		}

		lifecycleScope.launch {
			withContext(Dispatchers.IO) {
				vm.loadDevices()
			}
		}
	}
}
