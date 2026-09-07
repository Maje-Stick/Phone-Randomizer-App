package com.majestick.randomizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * A single-line field with a hairline border. Material's OutlinedTextField has a
 * 56dp minimum height, which is far too tall to stack twenty of.
 */
@Composable
private fun CompactField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier,
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .heightIn(min = 42.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                inner()
            }
        }
    )
}

/**
 * One row per entry: weight on the left in its own box, text beside it, remove
 * on the right. Weight defaults to 1, so an untouched list behaves like a plain
 * even-odds list.
 *
 * Tap a weight once and a nudge bar appears over that row; tap it again for the
 * keypad. Only one row can be active at a time, which is what closes the last
 * one when you move to the next.
 */
@Composable
fun EntryListEditor(
    entries: List<Entry>,
    onChange: (List<Entry>) -> Unit
) {
    var showPaste by remember { mutableStateOf(false) }
    var pasteText by remember { mutableStateOf("") }
    var activeIndex by remember { mutableStateOf(-1) }
    var editingIndex by remember { mutableStateOf(-1) }

    fun dismiss() {
        activeIndex = -1
        editingIndex = -1
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Taps on empty space in the editor close the nudge bar. Children
            // consume their own taps, so this only ever catches the gaps.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { dismiss() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Weight",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(58.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Entry  \u00b7  tap a weight to adjust it",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        entries.forEachIndexed { index, entry ->
            val active = index == activeIndex
            val editing = index == editingIndex

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    // Lift the active row so its nudge bar draws above, and
                    // takes touches ahead of, the rows around it.
                    .zIndex(if (active || editing) 1f else 0f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeightBox(
                        weight = entry.weight,
                        editing = editing,
                        highlighted = active || editing,
                        onTap = {
                            if (active) {
                                editingIndex = index
                                activeIndex = -1
                            } else {
                                activeIndex = index
                                editingIndex = -1
                            }
                        },
                        onWeightChange = {
                            onChange(entries.replaceAt(index, entry.copy(weight = it)))
                        },
                        onEditDone = { editingIndex = -1 },
                        modifier = Modifier.width(58.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    CompactField(
                        value = entry.text,
                        onValueChange = {
                            onChange(entries.replaceAt(index, entry.copy(text = it)))
                        },
                        placeholder = "Entry ${index + 1}",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            dismiss()
                            onChange(entries.filterIndexed { i, _ -> i != index })
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove entry ${index + 1}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                if (active) {
                    // Sits over the row, after it in the composition, so it
                    // draws on top and receives the touch instead of whatever
                    // is behind it.
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 70.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.94f),
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        RepeatIconButton(
                            icon = Icons.Filled.Remove,
                            description = "Decrease weight",
                            enabled = entry.weight > 0.0,
                            diameter = 32.dp
                        ) {
                            onChange(
                                entries.replaceAt(
                                    index,
                                    entry.copy(weight = (entry.weight - 1.0).coerceAtLeast(0.0))
                                )
                            )
                        }
                        RepeatIconButton(
                            icon = Icons.Filled.Add,
                            description = "Increase weight",
                            enabled = entry.weight < 9999.0,
                            diameter = 32.dp
                        ) {
                            onChange(
                                entries.replaceAt(
                                    index,
                                    entry.copy(weight = (entry.weight + 1.0).coerceAtMost(9999.0))
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    dismiss()
                    onChange(entries + Entry(""))
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add")
            }
            OutlinedButton(
                onClick = {
                    dismiss()
                    pasteText = ""
                    showPaste = true
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.ContentPaste,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Paste list")
            }
        }
    }

    if (showPaste) {
        AlertDialog(
            onDismissRequest = { showPaste = false },
            title = { Text("Paste a list") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        label = { Text("One per line") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Everything comes in at weight 1. Adjust individual weights afterwards.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = pasteText.isNotBlank(),
                    onClick = {
                        onChange(parseEntriesFromText(pasteText))
                        showPaste = false
                    }
                ) { Text("Replace list") }
            },
            dismissButton = {
                TextButton(onClick = { showPaste = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Controlled by the parent so only one row is ever active. Tap once to arm it,
 * tap again for the keypad.
 */
@Composable
private fun WeightBox(
    weight: Double,
    editing: Boolean,
    highlighted: Boolean,
    onTap: () -> Unit,
    onWeightChange: (Double) -> Unit,
    onEditDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var buffer by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val callback by rememberUpdatedState(onWeightChange)
    val done by rememberUpdatedState(onEditDone)

    LaunchedEffect(editing) {
        if (editing) buffer = trimNumber(weight)
    }

    fun commit() {
        buffer.toDoubleOrNull()?.let { callback(it.coerceIn(0.0, 9999.0)) }
        done()
    }

    Box(
        modifier = modifier
            .heightIn(min = 42.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .border(
                1.dp,
                if (highlighted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (editing) {
            BasicTextField(
                value = buffer,
                onValueChange = { raw -> buffer = raw.filter { it.isDigit() || it == '.' } },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { commit() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { if (!it.isFocused) commit() }
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        } else {
            Text(
                trimNumber(weight),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (highlighted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTap)
            )
        }
    }
}

private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    mapIndexed { i, existing -> if (i == index) value else existing }

/** 3.0 reads as "3"; 2.5 stays "2.5". */
fun trimNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else "%.2f".format(value).trimEnd('0').trimEnd('.')
