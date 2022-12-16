/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-15.
 */

package io.github.cloudcutter.ui.dialogs

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.cloudcutter.R
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LightleakStateDialog(private val context: Context) {

	private var continuation: Continuation<String?>? = null

	suspend fun show() = suspendCoroutine {
		continuation = it
		chooseStateInfoDialog()
	}

	private fun chooseStateInfoDialog() {
		MaterialAlertDialogBuilder(context)
			.setTitle(R.string.work_state_dialog_title)
			.setMessage(R.string.work_state_dialog_text)
			.setPositiveButton(R.string.ok) { _, _ ->
				chooseStateDialog()
			}
			.setNegativeButton(R.string.cancel) { _, _ ->
				continuation?.resume(null)
			}
			.setCancelable(false)
			.show()
	}

	private fun chooseStateDialog() {
		val items = listOf(
			R.string.work_state_dialog_raw,
			R.string.work_state_dialog_with_stager,
			R.string.work_state_dialog_running,
			R.string.work_state_dialog_connected,
		).map { context.getString(it) }

		MaterialAlertDialogBuilder(context)
			.setTitle(R.string.work_state_dialog_title)
			.setItems(items.toTypedArray()) { dialog, which ->
				dialog.dismiss()
				val actionId = when (which) {
					0 -> "message_custom_ap_connect"
					1 -> "message_device_connect_2"
					2 -> "message_device_connect_3"
					3 -> "work_state_running"
					else -> return@setItems
				}
				continuation?.resume(actionId)
			}
			.setNegativeButton(R.string.back) { _, _ ->
				chooseStateInfoDialog()
			}
			.setCancelable(false)
			.show()
	}
}
