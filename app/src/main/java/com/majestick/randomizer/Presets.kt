package com.majestick.randomizer

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
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
 * Entry point shown on each tool screen. Opens the manager; nothing destructive
 * is reachable without going through it first.
 */
@Composable
fun PresetBar(
    toolId: String,
    capture: () -> Map<String, String>,
    apply: (Map<String, String>) -> Unit
) {
    val context = LocalContext.current
    var presets by remember(toolId) { mutableStateOf(PresetStore.load(context, toolId)) }
    var loadedName by remember(toolId) { mutableStateOf<String?>(null) }
    var open by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { open = true },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.Bookmarks, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(10.dp))
        Text(
            loadedName?.let { "Preset: $it" } ?: "Presets",
            style = MaterialTheme.typography.titleMedium
        )
    }

    if (open) {
        PresetManagerDialog(
            presets = presets,
            onDismiss = { open = false },
            onLoad = { preset ->
                apply(preset.values)
                loadedName = preset.name
                open = false
            },
            onSave = { name ->
                val kept = presets.filterNot { it.name.equals(name, ignoreCase = true) }
                presets = (kept + Preset(name, capture())).sortedBy { it.name.lowercase() }
                PresetStore.save(context, toolId, presets)
                loadedName = name
            },
            onDelete = { name ->
                presets = presets.filterNot { it.name.equals(name, ignoreCase = true) }
                PresetStore.save(context, toolId, presets)
                if (loadedName.equals(name, ignoreCase = true)) loadedName = null
            }
        )
    }
}

/**
 * Tap a preset to select it, which copies its name into the field. Double tap to
 * load it outright. Save and Delete both confirm before doing anything.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetManagerDialog(
    presets: List<Preset>,
    onDismiss: () -> Unit,
    onLoad: (Preset) -> Unit,
    onSave: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var draftName by remember { mutableStateOf("") }
    var confirmOverwrite by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf<String?>(null) }

    val trimmed = draftName.trim()
    val matchesExisting = presets.any { it.name.equals(trimmed, ignoreCase = true) }
    val selectedPreset = presets.firstOrNull { it.name == selected }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Presets") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                if (presets.isEmpty()) {
                    Text(
                        "Nothing saved yet. Set the tool up, type a name below and hit Save.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 200.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            presets.forEach { preset ->
                                val isSelected = preset.name == selected
                                Text(
                                    preset.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .combinedClickable(
                                            onClick = {
                                                selected = preset.name
                                                draftName = preset.name
                                            },
                                            onDoubleClick = { onLoad(preset) }
                                        )
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tap to select, double tap to load.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    label = { Text("Preset name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (matchesExisting) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Saving will replace \"$trimmed\".",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { selectedPreset?.let(onLoad) },
                        enabled = selectedPreset != null,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Load") }

                    OutlinedButton(
                        onClick = { confirmDelete = selected },
                        enabled = selectedPreset != null,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Delete") }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = trimmed.isNotEmpty(),
                onClick = {
                    if (matchesExisting) confirmOverwrite = trimmed
                    else {
                        onSave(trimmed)
                        onDismiss()
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )

    confirmOverwrite?.let { name ->
        AlertDialog(
            onDismissRequest = { confirmOverwrite = null },
            title = { Text("Overwrite preset?") },
            text = { Text("\"$name\" already exists. Replace it with the current settings?") },
            confirmButton = {
                TextButton(onClick = {
                    onSave(name)
                    confirmOverwrite = null
                    onDismiss()
                }) { Text("Overwrite") }
            },
            dismissButton = {
                TextButton(onClick = { confirmOverwrite = null }) { Text("Cancel") }
            }
        )
    }

    confirmDelete?.let { name ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete preset?") },
            text = { Text("\"$name\" will be removed. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(name)
                    if (selected == name) selected = null
                    confirmDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancel") }
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
