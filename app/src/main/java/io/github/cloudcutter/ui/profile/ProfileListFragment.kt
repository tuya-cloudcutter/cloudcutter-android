/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import io.github.cloudcutter.databinding.ProfileListFragmentBinding
import io.github.cloudcutter.ui.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ProfileListFragment : BaseFragment<ProfileListFragmentBinding>({ inflater, parent ->
	ProfileListFragmentBinding.inflate(inflater, parent, false)
}) {

	override val vm: ProfileListViewModel by viewModels()

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		b.vm = vm

		b.list.addItemDecoration(MaterialDividerItemDecoration(
			requireContext(),
			MaterialDividerItemDecoration.VERTICAL,
		).also { it.isLastItemDecorated = false })
		b.list.layoutManager = LinearLayoutManager(context)

		vm.data.observe(viewLifecycleOwner) {
			b.list.adapter = ProfileListAdapter(it)
		}

		lifecycleScope.launch {
			withContext(Dispatchers.IO) {
				vm.loadData()
			}
		}
	}
}
