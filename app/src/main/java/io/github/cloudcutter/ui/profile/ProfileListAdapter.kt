/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.sizeDp
import io.github.cloudcutter.R
import io.github.cloudcutter.data.model.ProfileBase
import io.github.cloudcutter.databinding.ProfileListItemBinding
import io.github.cloudcutter.util.BindingViewHolder

class ProfileListAdapter(
	private val items: List<ProfileBase>,
) : RecyclerView.Adapter<BindingViewHolder<ProfileListItemBinding>>() {

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int,
	) = BindingViewHolder(
		ProfileListItemBinding.inflate(
			LayoutInflater.from(parent.context),
			parent,
			false,
		),
	)

	override fun onBindViewHolder(
		holder: BindingViewHolder<ProfileListItemBinding>,
		position: Int,
	) {
		val profile = items[position]
		holder.b.profile = profile
		holder.b.type.setText(when (profile.type) {
			ProfileBase.Type.CLASSIC -> R.string.profile_type_classic
			ProfileBase.Type.LIGHTLEAK -> R.string.profile_type_lightleak
		})
		holder.b.icon.icon = IconicsDrawable(
			context = holder.b.root.context,
			icon = profile.icon ?: CommunityMaterial.Icon.cmd_code_tags,
		).apply {
			sizeDp = 24
		}
	}

	override fun getItemCount() = items.size
}
