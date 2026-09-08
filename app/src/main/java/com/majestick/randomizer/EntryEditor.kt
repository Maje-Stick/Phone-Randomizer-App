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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf(false) }
    var buffer by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val nudge by rememberUpdatedState(onNudge)
    val callback by rememberUpdatedState(onSetWeight)
    val shape = RoundedCornerShape(10.dp)

    fun commit() {
        buffer.toDoubleOrNull()?.let { callback(it.coerceIn(0.0, 9999.0)) }
        editing = false
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
                .fillMaxHeight()
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
                    onValueChange = { raw -> buffer = raw.filter { it.isDigit() || it == '.' } },
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
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (!it.isFocused) commit() }
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
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
                            buffer = trimNumber(weight)
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
                .fillMaxHeight()
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
                    weight = entry.weight,
                    onNudge = { step ->
                        onChange { list ->
                            list.updateAt(index) {
                                it.copy(weight = (it.weight + step).coerceIn(0.0, 9999.0))
                            }
                        }
                    },
                    onSetWeight = { value ->
                        onChange { list -> list.updateAt(index) { it.copy(weight = value) } }
                    },
                    modifier = Modifier.width(WEIGHT_BAR_WIDTH)
                )
                Spacer(Modifier.width(8.dp))
                CompactField(
                    value = entry.text,
                    onValueChange = { text ->
                        onChange { list -> list.updateAt(index) { it.copy(text = text) } }
                    },
                    placeholder = "Entry ${index + 1}",
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onChange { list -> list.filterIndexed { i, _ -> i != index } } },
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
                onClick = { onChange { list -> list + Entry("") } },
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
                        "Edit, reorder, add or delete lines here and Apply writes them back "
                            + "to the rows. Weights travel with their text; a renamed or brand "
                            + "new line starts at 1.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onChange { previous -> mergeFromText(draftText, previous) }
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
 * Rebuilds the list from free text while keeping the weight already attached to
 * each piece of text, so editing the list as prose does not silently reset the
 * work done in the weight bars.
 */
private fun mergeFromText(text: String, previous: List<Entry>): List<Entry> {
    val weights = HashMap<String, Double>()
    previous.forEach { entry ->
        if (entry.text.isNotBlank()) weights.putIfAbsent(entry.text, entry.weight)
    }
    val lines = text.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
    if (lines.isEmpty()) return listOf(Entry(""))
    return lines.map { line -> Entry(line, weights[line] ?: 1.0) }
}

private fun List<Entry>.updateAt(index: Int, transform: (Entry) -> Entry): List<Entry> =
    if (index !in indices) this
    else mapIndexed { i, existing -> if (i == index) transform(existing) else existing }

/** 3.0 reads as "3"; 2.5 stays "2.5". */
fun trimNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString()
    else "%.2f".format(value).trimEnd('0').trimEnd('.')
