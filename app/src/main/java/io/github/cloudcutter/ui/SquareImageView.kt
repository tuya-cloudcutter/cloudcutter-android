/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-1.
 */

package io.github.cloudcutter.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class SquareImageView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyle: Int = 0,
) : AppCompatImageView(context, attrs, defStyle) {

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		setMeasuredDimension(measuredWidth, measuredWidth)
	}
}
