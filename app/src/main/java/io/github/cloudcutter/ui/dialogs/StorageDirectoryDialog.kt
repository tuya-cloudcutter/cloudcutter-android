/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-15.
 */

package io.github.cloudcutter.ui.dialogs

import android.content.Context
import android.text.InputType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.cloudcutter.R
import io.github.cloudcutter.ext.input
import io.github.cloudcutter.ext.openChild
import io.github.cloudcutter.ext.toIsoString
import java.io.File
import java.time.LocalDateTime
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StorageDirectoryDialog(private val context: Context) {

	private var continuation: Continuation<File?>? = null

	suspend fun show() = suspendCoroutine {
		continuation = it
		chooseDirectoryInfoDialog()
	}

	private fun chooseDirectoryInfoDialog() {
		MaterialAlertDialogBuilder(context)
			.setTitle(R.string.work_directory_dialog_title)
			.setMessage(R.string.work_directory_dialog_text)
			.setPositiveButton(R.string.ok) { _, _ ->
				chooseDirectoryDialog()
			}
			.setNegativeButton(R.string.cancel) { _, _ ->
				continuation?.resume(null)
			}
			.setCancelable(false)
			.show()
	}

	private fun chooseDirectoryDialog() {
		val filesDir = context.getExternalFilesDir(null) ?: context.filesDir
		val now = LocalDateTime.now().toIsoString()
		val deviceDirs = filesDir.listFiles { it: File ->
			it.isDirectory
		}?.sortedBy { it.name } ?: listOf<File>()
		// list all saved devices, current date and "add device" item
		val items = deviceDirs + filesDir.openChild("${now}_lightleak") + null

		MaterialAlertDialogBuilder(context)
			.setTitle(R.string.work_directory_dialog_title)
			.setItems(items.map { directory ->
				directory?.name ?: context.getString(R.string.work_directory_dialog_add)
			}.toTypedArray()) { dialog, which ->
				val item = items[which]
				if (item == null) {
					dialog.dismiss()
					chooseDirectoryNameDialog()
					return@setItems
				}
				continuation?.resume(item)
			}
			.setNegativeButton(R.string.back) { _, _ ->
				chooseDirectoryInfoDialog()
			}
			.setCancelable(false)
			.show()
	}

	private fun chooseDirectoryNameDialog() {
		MaterialAlertDialogBuilder(context)
			.setTitle(R.string.work_directory_name_dialog_title)
			.input(
				type = InputType.TYPE_CLASS_TEXT,
				positiveButton = R.string.ok,
				positiveListener = { _, input ->
					if (input.isEmpty())
						return@input false
					val name = input.lowercase()
						.trim()
						.replace(" ", "-")
						.replace("_", "-")
						.replace("[^a-z0-9-]".toRegex(), "")
						.replace("-+".toRegex(), "-")
						.trim('-')
					val filesDir = context.getExternalFilesDir(null) ?: context.filesDir
					filesDir.openChild(name).mkdirs()
					return@input true
				},
			)
			.setNegativeButton(R.string.cancel, null)
			.setOnDismissListener {
				chooseDirectoryDialog()
			}
			.setCancelable(false)
			.show()
	}
}
