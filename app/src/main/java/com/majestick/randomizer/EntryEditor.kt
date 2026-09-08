package com.majestick.randomizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ROW_HEIGHT = 42.dp
private val WEIGHT_BAR_WIDTH = 88.dp

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
                    .heightIn(min = ROW_HEIGHT)
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
 * The weight control: minus and plus built into the ends of the box itself, the
 * number in the middle. Always visible, no modes, nothing to open or dismiss.
 * Tap or hold the ends to change it; tap the number to type one.
 */
@Composable
private fun WeightBar(
    weight: Double,
    onNudge: (Int) -> Unit,
    onSetWeight: (Double) -> Unit,
    modifier: Modifier = Modifier,
    barName: String = "weight"
) {
    var editing by remember { mutableStateOf(false) }
    var buffer by remember { mutableStateOf(TextFieldValue("")) }
    var gainedFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val nudge by rememberUpdatedState(onNudge)
    val callback by rememberUpdatedState(onSetWeight)
    val shape = RoundedCornerShape(10.dp)

    fun commit() {
        val parsed = buffer.text.toDoubleOrNull()
        DebugLog.trace("keypad") { "weight commit buffer='${buffer.text}' parsed=$parsed" }
        parsed?.let { callback(it.coerceIn(0.0, 9999.0)) }
        editing = false
        keyboard?.hide()
    }

    Row(
        modifier = modifier
            .height(ROW_HEIGHT)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(
                1.dp,
                if (editing) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RepeatPressBox(
            enabled = weight > 0.0,
            onStep = { nudge(-1) },
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            name = "$barName minus"
        ) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = "Decrease weight",
                tint = if (weight > 0.0) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(13.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            if (editing) {
                BasicTextField(
                    value = buffer,
                    onValueChange = { raw ->
                        val clean = raw.text.filter { it.isDigit() || it == '.' }
                        buffer = if (clean == raw.text) raw
                        else TextFieldValue(clean, TextRange(clean.length))
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        commit()
                        focus.clearFocus()
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            DebugLog.trace("keypad") { "weight focus isFocused=${state.isFocused} hasFocus=${state.hasFocus} gainedBefore=$gainedFocus" }
                            if (state.isFocused) {
                                gainedFocus = true
                                keyboard?.show()
                            } else if (gainedFocus && editing) {
                                commit()
                            }
                        }
                )
                LaunchedEffect(Unit) {
                    DebugLog.trace("keypad") { "weight field composed, controller=${if (keyboard == null) "NULL" else "ok"}" }
                    withFrameNanos { }
                    runCatching { focusRequester.requestFocus() }
                        .onSuccess { DebugLog.trace("keypad") { "weight requestFocus sent" } }
                        .onFailure { DebugLog.log("keypad", "weight requestFocus FAILED: $it") }
                    keyboard?.show()
                }
            } else {
                Text(
                    trimNumber(weight),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .clickable {
                            DebugLog.trace("keypad") { "weight tapped, value=${trimNumber(weight)} -> opening editor" }
                            val start = trimNumber(weight)
                            buffer = TextFieldValue(start, TextRange(start.length))
                            gainedFocus = false
                            editing = true
                        }
                        .padding(top = 11.dp)
                )
            }
        }

        RepeatPressBox(
            enabled = weight < 9999.0,
            onStep = { nudge(1) },
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            name = "$barName plus"
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Increase weight",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

/**
 * One row per entry: the weight bar on the left, text beside it, remove on the
 * right. Weight defaults to 1, so an untouched list is plain even odds.
 *
 * [onChange] takes a transform rather than a finished list on purpose. A held
 * plus button fires faster than recomposition, so a captured snapshot of the
 * list goes stale between presses and writes back an old value -- which is
 * exactly the increment-then-revert behaviour. Applying a transform to whatever
 * the list is at that instant cannot go stale.
 */
@Composable
fun EntryListEditor(
    entries: List<Entry>,
    onChange: ((List<Entry>) -> List<Entry>) -> Unit
) {
    var showText by remember { mutableStateOf(false) }
    var draftText by remember { mutableStateOf("") }

    // Every mutation of the list, with the weights that came out. This is what
    // makes an increment-then-revert visible as two entries instead of one.
    val emit: (((List<Entry>) -> List<Entry>) -> Unit) = { transform ->
        onChange { list ->
            val out = transform(list)
            DebugLog.trace("entries") {
                "${list.size} rows -> ${out.size}, weights [" +
                    out.joinToString(",") { trimNumber(it.weight) } + "]"
            }
            out
        }
    }

    Column(Modifier.fillMaxWidth()) {
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
                modifier = Modifier.width(WEIGHT_BAR_WIDTH)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Entry",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        entries.forEachIndexed { index, entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeightBar(
                    barName = "weight[$index]",
                    weight = entry.weight,
                    onNudge = { step ->
                        emit { list ->
                            list.updateAt(index) {
                                it.copy(weight = (it.weight + step).coerceIn(0.0, 9999.0))
                            }
                        }
                    },
                    onSetWeight = { value ->
                        emit { list -> list.updateAt(index) { it.copy(weight = value) } }
                    },
                    modifier = Modifier.width(WEIGHT_BAR_WIDTH)
                )
                Spacer(Modifier.width(8.dp))
                CompactField(
                    value = entry.text,
                    onValueChange = { text ->
                        emit { list -> list.updateAt(index) { it.copy(text = text) } }
                    },
                    placeholder = "Entry ${index + 1}",
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { emit { list -> list.filterIndexed { i, _ -> i != index } } },
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
        }

        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { emit { list -> list + Entry("") } },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add")
            }
            OutlinedButton(
                onClick = {
                    draftText = entries.joinToString("\n") { it.text }
                    showText = true
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
                Text("Text view")
            }
        }
    }

    if (showText) {
        AlertDialog(
            onDismissRequest = { showText = false },
            title = { Text("All entries") },
            text = {
                Column {
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { draftText = it },
                        label = { Text("One per line") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Edit, reorder, rename, add or delete lines here and Apply writes "
                            + "them back to the rows. Weights follow their row, so reordering "
                            + "and renaming both keep them. Only a brand new line starts at 1.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    emit { previous -> mergeFromText(draftText, previous) }
                    showText = false
                }) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { showText = false }) { Text("Cancel") }
            }
        )
    }
}

/**
 * Rebuilds the list from free text without throwing away the weights.
 *
 * Two passes, because text alone cannot say what happened. Pass one matches
 * lines to old entries by exact text, each old entry claimed at most once --
 * that covers reordering, where the same words moved. Pass two gives any
 * still-unmatched line the weight of the old entry that sat at its position, if
 * that entry was not already claimed -- that covers renaming in place, where
 * the words changed but the row did not. Only a genuinely new line starts at 1.
 */
private fun mergeFromText(text: String, previous: List<Entry>): List<Entry> {
    val lines = text.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return listOf(Entry(""))

    val unclaimed = previous.toMutableList()
    val matched = arrayOfNulls<Entry>(lines.size)

    lines.forEachIndexed { i, line ->
        val hit = unclaimed.indexOfFirst { it.text == line }
        if (hit >= 0) {
            matched[i] = Entry(line, unclaimed[hit].weight)
            unclaimed.removeAt(hit)
        }
    }

    lines.forEachIndexed { i, line ->
        if (matched[i] == null) {
            val sameRow = previous.getOrNull(i)
            if (sameRow != null && unclaimed.remove(sameRow)) {
                matched[i] = Entry(line, sameRow.weight)
            }
        }
    }

    return lines.mapIndexed { i, line -> matched[i] ?: Entry(line, 1.0) }
}

private fun List<Entry>.updateAt(index: Int, transform: (Entry) -> Entry): List<Entry> =
    if (index !in indices) this
    else mapIndexed { i, existing -> if (i == index) transform(existing) else existing }

/** 3.0 reads as "3"; 2.5 stays "2.5". */
fun trimNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else "%.2f".format(value).trimEnd('0').trimEnd('.')
