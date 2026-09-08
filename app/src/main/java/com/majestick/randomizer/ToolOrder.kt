package com.majestick.randomizer

import android.content.Context

/**
 * The order of the tools on the home grid.
 *
 * Stored as a list of ids rather than positions, so a future release that adds
 * or drops a tool does not scramble an order the user arranged by hand. Ids that
 * no longer exist are dropped on load, and tools the saved order has never seen
 * are appended in their built-in order.
 */
object ToolOrderStore {
    private const val PREFS = "randomizer_settings"
    private const val KEY = "tool_order"

    fun load(context: Context, defaults: List<String>): List<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return defaults
        val saved = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val known = saved.filter { it in defaults }.distinct()
        return known + defaults.filterNot { it in known }
    }

    fun save(context: Context, order: List<String>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, order.joinToString(",")).apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY).apply()
    }
}
