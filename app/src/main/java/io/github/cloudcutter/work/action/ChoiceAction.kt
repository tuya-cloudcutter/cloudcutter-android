/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text

class ChoiceAction(
	id: String,
	title: Text,
	val key: String,
	val choices: List<Choice>,
) : Action(id, title, null) {

	data class Choice(
		val id: Int,
		val name: Text,
		val nextId: Int,
		val value: Any?,
	)
}
