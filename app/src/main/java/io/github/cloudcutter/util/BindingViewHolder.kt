/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.util

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BindingViewHolder<B : ViewDataBinding>(val b: B) : RecyclerView.ViewHolder(b.root)
