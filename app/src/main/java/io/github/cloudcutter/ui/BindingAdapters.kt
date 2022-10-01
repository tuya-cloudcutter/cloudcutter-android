/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.Transformation

@BindingAdapter("android:isVisible")
fun bindIsVisible(view: View, isVisible: Boolean) {
	view.isVisible = isVisible
}

@BindingAdapter("android:isInvisible")
fun bindIsInvisible(view: View, isInvisible: Boolean) {
	view.isInvisible = isInvisible
}

@BindingAdapter("android:url")
fun bindUrl(imageView: ImageView, url: String?) {
	imageView.load(url) {
		crossfade(true)
		placeholder(ColorDrawable(Color.TRANSPARENT))
	}
}

@BindingAdapter("android:urlCircled")
fun bindUrlCircled(imageView: ImageView, url: String?) {
	imageView.load(url) {
		crossfade(true)
		transformations(CircleCropTransformation())
		placeholder(ColorDrawable(Color.TRANSPARENT))
	}
}
