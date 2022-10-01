/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import io.github.cloudcutter.ui.main.MainViewModel

abstract class BaseFragment<B : ViewBinding>(
	private val inflater: (inflater: LayoutInflater, parent: ViewGroup?) -> B,
) : Fragment() {

	protected lateinit var b: B
	protected abstract val vm: BaseViewModel
	protected val activity: MainViewModel by activityViewModels()

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
	}
}
