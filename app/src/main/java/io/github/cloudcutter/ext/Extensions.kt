/*
 * Copyright (c) Kuba Szczodrzyński 2022-9-27.
 */

package io.github.cloudcutter.ext

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

operator fun MatchResult.get(i: Int) = this.groupValues[i]

@OptIn(ExperimentalContracts::class)
suspend fun <R> runFinally(finally: suspend () -> Unit, block: suspend () -> R): R {
	contract {
		callsInPlace(block, InvocationKind.EXACTLY_ONCE)
		callsInPlace(finally, InvocationKind.EXACTLY_ONCE)
	}
	return try {
		val result = block()
		finally()
		result
	} catch (e: Exception) {
		finally()
		throw e
	}
}
