package com.majestick.randomizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty

/**
 * Tool state lives here rather than inside each composable.
 *
 * Because this is a singleton, values survive leaving a tool and coming back.
 * Because it is only in memory, everything is gone when the process dies --
 * which is exactly the lifetime asked for: sticky across navigation, cleared on
 * a real app exit. Presets are the mechanism for anything meant to outlive that.
 */
object Drafts {
    val values = mutableStateMapOf<String, Any?>()

    fun clear() = values.clear()
}

class Draft<T>(private val key: String, private val initial: T) {

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
        (Drafts.values[key] as? T) ?: initial

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        Drafts.values[key] = value
    }
}

/**
 * Drop-in replacement for `remember { mutableStateOf(x) }` that outlives the
 * composable. Key must be unique across the app -- prefix it with the tool id.
 */
@Composable
fun <T> draft(key: String, initial: T): Draft<T> = remember(key) { Draft(key, initial) }
