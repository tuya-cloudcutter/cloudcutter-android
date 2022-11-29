package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text

class WorkStateAction(
	id: String,
	val text: Text,
	nextId: String?,
) : Action(id, null, nextId)
