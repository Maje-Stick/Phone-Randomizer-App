package com.majestick.randomizer

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

/**
 * A saved snapshot of one tool's settings. Values are stored as strings so a
 * single storage format covers every tool regardless of its state shape.
 */
data class Preset(val name: String, val values: Map<String, String>)

/**
 * Presets are scoped per tool: a preset saved in Shuffle never shows up in Pick.
 * Backed by SharedPreferences and org.json, both part of the Android platform,
 * so this adds no dependencies to the build.
 */
object PresetStore {

    private const val PREFS = "randomizer_presets"

    fun load(context: Context, toolId: String): List<Preset> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(toolId, null) ?: return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val entry = array.getJSONObject(i)
                val values = entry.getJSONObject("values")
                Preset(
                    name = entry.getString("name"),
                    values = values.keys().asSequence().associateWith { values.getString(it) }
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, toolId: String, presets: List<Preset>) {
        val array = JSONArray()
        presets.forEach { preset ->
            val values = JSONObject()
            preset.values.forEach { (key, value) -> values.put(key, value) }
            array.put(JSONObject().put("name", preset.name).put("values", values))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(toolId, array.toString())
            .apply()
    }
}

/**
 * Tap a chip to load a preset, the x to delete it. Saving under an existing
 * name overwrites it, which doubles as "update".
 *
 * @param capture reads the tool's current settings into a storable map
 * @param apply pushes a stored map back into the tool's state
 */
@Composable
fun PresetBar(
    toolId: String,
    capture: () -> Map<String, String>,
    apply: (Map<String, String>) -> Unit
) {
    val context = LocalContext.current
    var presets by remember(toolId) { mutableStateOf(PresetStore.load(context, toolId)) }
    var showDialog by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Presets",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = {
                draftName = ""
                showDialog = true
            }) {
                Text("Save current")
            }
        }

        if (presets.isEmpty()) {
            Text(
                "Set this tool up how you like, then save it here.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    AssistChip(
                        onClick = { apply(preset.values) },
                        label = { Text(preset.name) },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Delete ${preset.name}",
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        presets = presets.filterNot { it.name == preset.name }
                                        PresetStore.save(context, toolId, presets)
                                    }
                            )
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        val trimmed = draftName.trim()
        val willOverwrite = presets.any { it.name.equals(trimmed, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Save preset") },
            text = {
                Column {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    if (willOverwrite) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "A preset called \"$trimmed\" already exists. Saving replaces it.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = trimmed.isNotEmpty(),
                    onClick = {
                        val kept = presets.filterNot { it.name.equals(trimmed, ignoreCase = true) }
                        presets = (kept + Preset(trimmed, capture()))
                            .sortedBy { it.name.lowercase() }
                        PresetStore.save(context, toolId, presets)
                        showDialog = false
                    }
                ) {
                    Text(if (willOverwrite) "Overwrite" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/** Small helpers so each screen's apply() stays one line per field. */
fun Map<String, String>.str(key: String, fallback: String): String = this[key] ?: fallback

fun Map<String, String>.int(key: String, fallback: Int): Int =
    this[key]?.toIntOrNull() ?: fallback

fun Map<String, String>.bool(key: String, fallback: Boolean): Boolean =
    this[key]?.toBooleanStrictOrNull() ?: fallback
