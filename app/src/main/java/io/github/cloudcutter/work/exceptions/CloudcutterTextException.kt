/*
 * Copyright (c) Kuba Szczodrzyński 2022-12-14.
 */

package io.github.cloudcutter.work.exceptions

import io.github.cloudcutter.util.Text

open class CloudcutterTextException(val text: Text) : CloudcutterException("Cloudcutter exception $text")
