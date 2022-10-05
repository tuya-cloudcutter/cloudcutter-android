/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-3.
 */

package io.github.cloudcutter.ui.work

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import io.github.cloudcutter.R
import io.github.cloudcutter.databinding.WorkItemBinding
import io.github.cloudcutter.util.BindingViewHolder
import io.github.cloudcutter.work.ActionState

class WorkAdapter(
	private val items: List<ActionState>,
) : RecyclerView.Adapter<BindingViewHolder<WorkItemBinding>>() {

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	) = BindingViewHolder(
		WorkItemBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false,
		),
	)

	override fun onBindViewHolder(holder: BindingViewHolder<WorkItemBinding>, position: Int) {
		val state = items[position]
		val icon = if (state.error == null)
			CommunityMaterial.Icon.cmd_check_circle_outline
		else
			CommunityMaterial.Icon.cmd_alert_circle_outline
		holder.b.state = state
		if (state.progress) {
			holder.b.icon.icon = null
			return
		}
		holder.b.icon.icon = IconicsDrawable(holder.b.root.context, icon).apply {
			sizeDp = 32
			colorRes = if (state.error == null) R.color.icon_success else R.color.icon_error
		}
	}

	override fun getItemCount() = items.size
}
