/*
 * Copyright (c) Kuba Szczodrzyński 2022-10-2.
 */

package io.github.cloudcutter.work.action

import io.github.cloudcutter.util.Text

class DummyAction(
	title: Text,
) : Action("dummy_${System.currentTimeMillis()}", title, null, null)
