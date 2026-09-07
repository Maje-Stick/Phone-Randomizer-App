package com.majestick.randomizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScaffold(
    title: String,
    onBack: () -> Unit,
    actionLabel: String,
    onAction: () -> Unit,
    content: @Composable () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .height(58.dp)
            ) {
                Text(actionLabel, style = MaterialTheme.typography.titleMedium)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Fires once on press, then repeats while held. Holding twenty taps' worth of
 * increments is the whole point -- tapping fifty times is not a UI.
 */
@Composable
private fun RepeatIconButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onStep: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val step by rememberUpdatedState(onStep)
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.background,
                CircleShape
            )
            .border(
                1.dp,
                if (enabled) MaterialTheme.colorScheme.outline
                else MaterialTheme.colorScheme.background,
                CircleShape
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        step()
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val job = scope.launch {
                            delay(450)
                            while (true) {
                                step()
                                delay(70)
                            }
                        }
                        tryAwaitRelease()
                        job.cancel()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * A number you can scrub. Tap it to type an exact value; press and hold for
 * ~300ms to enter scrub mode, then the side of the box your finger sits on
 * decides direction and how far out decides speed.
 *
 * Position-based rather than delta-based on purpose: a delta drag runs out of
 * screen, and running out of screen should not mean running out of numbers.
 * Once scrubbing, the finger can travel anywhere -- only which side it is on
 * matters.
 *
 * Both gestures live in ONE pointerInput. Two separate handlers on the same
 * element fight, and the drag one wins, which is why tapping stopped working.
 */
@Composable
fun ScrubbableText(
    text: String,
    onTapToEdit: () -> Unit,
    onNudge: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default
) {
    var scrubbing by remember { mutableStateOf(false) }
    val pointerX = remember { mutableStateOf(0f) }
    val boxWidth = remember { mutableStateOf(1f) }

    val nudge by rememberUpdatedState(onNudge)
    val tap by rememberUpdatedState(onTapToEdit)
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(scrubbing) {
        if (!scrubbing) return@LaunchedEffect
        while (true) {
            val half = (boxWidth.value / 2f).coerceAtLeast(1f)
            val offset = pointerX.value - half
            val reach = (abs(offset) / half).coerceIn(0f, 1.6f)
            if (reach > 0.18f) {
                nudge(if (offset > 0f) 1 else -1)
                val speed = ((reach - 0.18f) / 1.42f).coerceIn(0f, 1f)
                delay((200f - 175f * speed).toLong())
            } else {
                delay(60)
            }
        }
    }

    Text(
        text,
        style = style,
        color = if (scrubbing) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    boxWidth.value = size.width.toFloat()
                    pointerX.value = down.position.x

                    var releasedEarly = false
                    val settled = withTimeoutOrNull(300L) {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null) {
                                releasedEarly = true
                                break
                            }
                            pointerX.value = change.position.x
                            if (!change.pressed) {
                                releasedEarly = true
                                break
                            }
                        }
                    }

                    if (settled != null && releasedEarly) {
                        tap()
                    } else {
                        scrubbing = true
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            pointerX.value = change.position.x
                            change.consume()
                            if (!change.pressed) break
                        }
                        scrubbing = false
                    }
                }
            }
        }
    )
}

/**
 * Shows a number. Tap to type it, hold to scrub it.
 */
@Composable
fun EditableNumber(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf(false) }
    var buffer by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val current by rememberUpdatedState(value)
    val callback by rememberUpdatedState(onValueChange)

    fun commit() {
        buffer.toIntOrNull()?.let { callback(it.coerceIn(min, max)) }
        editing = false
    }

    if (editing) {
        BasicTextField(
            value = buffer,
            onValueChange = { raw -> buffer = raw.filter { it.isDigit() || it == '-' } },
            singleLine = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.primary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { commit() }),
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { if (!it.isFocused && editing) commit() }
        )
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    } else {
        ScrubbableText(
            text = value.toString(),
            onTapToEdit = {
                buffer = current.toString()
                editing = true
            },
            onNudge = { step -> callback((current + step).coerceIn(min, max)) },
            modifier = modifier,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun Stepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 1,
    max: Int = 999,
    step: Int = 1
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        RepeatIconButton(
            icon = Icons.Filled.Remove,
            description = "Decrease $label",
            enabled = value > min
        ) { onValueChange((value - step).coerceIn(min, max)) }

        EditableNumber(
            value = value,
            onValueChange = onValueChange,
            min = min,
            max = max,
            modifier = Modifier
                .width(72.dp)
                .padding(horizontal = 4.dp)
        )

        RepeatIconButton(
            icon = Icons.Filled.Add,
            description = "Increase $label",
            enabled = value < max
        ) { onValueChange((value + step).coerceIn(min, max)) }
    }
}

@Composable
fun NumberInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new -> onValueChange(new.filter { it.isDigit() || it == '-' || it == '.' }) },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun ItemsInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Your items",
    hint: String = "One per line, or separated by commas"
) {
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            hint,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SectionSpacer() = Spacer(Modifier.height(20.dp))
