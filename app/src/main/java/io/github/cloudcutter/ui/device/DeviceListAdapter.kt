/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.ui.device

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.cloudcutter.data.model.DeviceBase
import io.github.cloudcutter.databinding.DeviceListItemBinding
import io.github.cloudcutter.util.BindingViewHolder

class DeviceListAdapter(
	private val items: List<DeviceBase>,
) : RecyclerView.Adapter<BindingViewHolder<DeviceListItemBinding>>() {

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	) = BindingViewHolder(
		DeviceListItemBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false,
		),
	)

	override fun onBindViewHolder(holder: BindingViewHolder<DeviceListItemBinding>, position: Int) {
		holder.b.device = items[position]
	}

	override fun getItemCount() = items.size
}
