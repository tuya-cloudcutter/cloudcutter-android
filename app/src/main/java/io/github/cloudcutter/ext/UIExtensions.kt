/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.ext

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import androidx.activity.ComponentActivity
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.google.android.material.button.MaterialButton
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.colorRes
import com.mikepenz.iconics.utils.sizeDp
import io.github.cloudcutter.R
import io.github.cloudcutter.databinding.LayoutMessageCardBinding
import io.github.cloudcutter.ui.base.BaseFragment
import io.github.cloudcutter.work.exceptions.CloudcutterException
import io.github.cloudcutter.work.exceptions.CloudcutterTextException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

inline fun <reified B : ViewBinding> ComponentActivity.viewBinding(
	crossinline inflater: (LayoutInflater) -> B,
) = lazy(LazyThreadSafetyMode.NONE) {
	inflater(layoutInflater)
}

@ColorInt
fun @receiver:AttrRes Int.resolveAttr(context: Context?): Int {
	val typedValue = TypedValue()
	context?.theme?.resolveAttribute(this, typedValue, true)
	val typedArray = context?.obtainStyledAttributes(typedValue.data, intArrayOf(this))
	val color = typedArray?.getColor(0, Color.TRANSPARENT)
	typedArray?.recycle()
	return color ?: Color.TRANSPARENT
}

@Suppress("UNCHECKED_CAST")
inline fun <T : View> T.onClick(crossinline onClickListener: (v: T) -> Unit) {
	setOnClickListener { v: View ->
		onClickListener(v as T)
	}
}

@Suppress("UNCHECKED_CAST")
inline fun <T : View> T.onLongClick(crossinline onLongClickListener: (v: T) -> Boolean) {
	setOnLongClickListener { v: View ->
		onLongClickListener(v as T)
	}
}

@Suppress("UNCHECKED_CAST")
inline fun <T : CompoundButton> T.onChange(crossinline onChangeListener: (v: T, isChecked: Boolean) -> Unit) {
	setOnCheckedChangeListener { buttonView, isChecked ->
		onChangeListener(buttonView as T, isChecked)
	}
}

@Suppress("UNCHECKED_CAST")
inline fun <T : MaterialButton> T.onChange(crossinline onChangeListener: (v: T, isChecked: Boolean) -> Unit) {
	clearOnCheckedChangeListeners()
	addOnCheckedChangeListener { buttonView, isChecked ->
		onChangeListener(buttonView as T, isChecked)
	}
}

fun BaseFragment<*>.launchWithErrorCard(
	b: LayoutMessageCardBinding,
	block: suspend CoroutineScope.() -> Unit,
) = lifecycleScope.launch {
	try {
		block()
	} catch (e: Exception) {
		val context = b.root.context
		val text = when (e) {
			is CloudcutterTextException -> e.text.format(context)
			is CloudcutterException -> e.message
			else -> "Exception: ${e::class.java}\n\n${e.message}"
		}
		log("Error: $text")
		b.messageCard.isVisible = true
		b.messageTitle.setText(R.string.message_title_error)
		b.messageText.text = text
		b.messageIcon.icon = IconicsDrawable(context, CommunityMaterial.Icon.cmd_alert_circle_outline).apply {
			sizeDp = 32
			colorRes = R.color.icon_error
		}
	}
}
