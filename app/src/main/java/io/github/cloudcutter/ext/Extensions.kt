/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.ext

operator fun MatchResult.get(i: Int) = this.groupValues[i]


