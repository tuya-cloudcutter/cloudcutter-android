/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-28.
 */

package io.github.cloudcutter.ui

import android.text.InputType
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import io.github.cloudcutter.databinding.DialogEditTextBinding

public inline fun <reified B : ViewBinding> ComponentActivity.viewBinding(
	crossinline inflater: (LayoutInflater) -> B,
) = lazy(LazyThreadSafetyMode.NONE) {
	inflater(layoutInflater)
}

fun MaterialAlertDialogBuilder.input(
	message: CharSequence? = null,
	type: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE,
	hint: CharSequence? = null,
	value: CharSequence? = null,
	changeListener: ((editText: TextInputEditText, input: String) -> Boolean)? = null,
	positiveButton: Int? = null,
	positiveListener: ((editText: TextInputEditText, input: String) -> Boolean)? = null,
): MaterialAlertDialogBuilder {
	val b = DialogEditTextBinding.inflate(LayoutInflater.from(context), null, false)
	b.title.text = message
	b.title.isVisible = message != null && message.isNotEmpty()
	b.text1.hint = hint
	b.text1.inputType = type
	b.text1.setText(value)
	b.text1.addTextChangedListener { text ->
		if (changeListener?.invoke(b.text1, text?.toString() ?: "") != false)
			b.text1.error = null
	}
	if (positiveButton != null) {
		setPositiveButton(positiveButton) { dialog, _ ->
			if (positiveListener?.invoke(b.text1, b.text1.text?.toString() ?: "") != false)
				dialog.dismiss()
		}
	}
	setView(b.root)

	return this
}
