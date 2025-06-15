package androidx.picker.helper

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.runBlocking

/**
 * Collects the given [Flow] synchronously in a blocking manner.
 * Used for triggering side effects or ensuring the flow is fully collected.
 */
fun <T> Flow<T>.loadIconSync() = runBlocking { lastOrNull() as? T? }
