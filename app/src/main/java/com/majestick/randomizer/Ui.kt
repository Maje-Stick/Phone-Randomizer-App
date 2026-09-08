package com.majestick.randomizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val focusManager = LocalFocusManager.current
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
                    DebugLog.log("action", "$title -> $actionLabel")
                    focusManager.clearFocus()
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
 * Press-and-hold behaviour with no styling of its own, so callers can shape it.
 * Used for the flat segments at each end of a weight bar.
 */
@Composable
fun RepeatPressBox(
    enabled: Boolean,
    onStep: () -> Unit,
    modifier: Modifier = Modifier,
    name: String = "press",
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val step by rememberUpdatedState(onStep)
    val active by rememberUpdatedState(enabled)
    val haptics = LocalHapticFeedback.current
    val focus = LocalFocusManager.current

    LaunchedEffect(enabled) { DebugLog.trace("gesture", "$name enabled=$enabled") }
    DisposableEffect(Unit) {
        DebugLog.trace("gesture", "$name attached")
        onDispose { DebugLog.trace("gesture", "$name disposed") }
    }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    DebugLog.trace("gesture", "$name DOWN (enabled=$active)")
                    // Commits and dismisses any open number editor first, so the
                    // button acts on the committed value instead of fighting it.
                    focus.clearFocus()
                    if (active) {
                        step()
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val job = scope.launch {
                            delay(450)
                            var n = 0
                            while (active) {
                                n++
                                if (n <= 3 || n % 10 == 0) {
                                    DebugLog.trace("gesture", "$name repeat #$n")
                                }
                                step()
                                delay(70)
                            }
                            DebugLog.trace("gesture", "$name repeat loop ended after $n (enabled went false)")
                        }
                        try {
                            val released = tryAwaitRelease()
                            DebugLog.trace("gesture", "$name UP released=$released")
                        } finally {
                            DebugLog.trace("gesture", "$name cancelling repeat job (wasActive=${job.isActive})")
                            job.cancel()
                        }
                    }
                }
            )
        },
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Fires once on press, then repeats while held. Holding twenty taps' worth of
 * increments is the whole point -- tapping fifty times is not a UI.
 */
@Composable
fun RepeatIconButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    diameter: Dp = 40.dp,
    onStep: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val step by rememberUpdatedState(onStep)
    val active by rememberUpdatedState(enabled)
    val haptics = LocalHapticFeedback.current
    val focus = LocalFocusManager.current

    LaunchedEffect(enabled) { DebugLog.trace("gesture", "$description enabled=$enabled") }
    DisposableEffect(Unit) {
        DebugLog.trace("gesture", "$description attached")
        onDispose { DebugLog.trace("gesture", "$description disposed") }
    }

    Box(
        modifier = Modifier
            .size(diameter)
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
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        DebugLog.trace("gesture", "$description DOWN (enabled=$active)")
                        focus.clearFocus()
                        if (active) {
                            step()
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val job = scope.launch {
                                delay(450)
                                var n = 0
                                while (active) {
                                    n++
                                    if (n <= 3 || n % 10 == 0) {
                                        DebugLog.trace("gesture", "$description repeat #$n")
                                    }
                                    step()
                                    delay(70)
                                }
                                DebugLog.trace("gesture", "$description repeat loop ended after $n (enabled went false)")
                            }
                            try {
                                val released = tryAwaitRelease()
                                DebugLog.trace("gesture", "$description UP released=$released")
                            } finally {
                                DebugLog.trace("gesture", "$description cancelling repeat job (wasActive=${job.isActive})")
                                job.cancel()
                            }
                        }
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
 * Shows a number. Tap it to type an exact value on the keypad.
 *
 * Deliberately a plain `clickable` rather than a custom gesture: this sits
 * inside a vertically scrolling column, and hand-rolled pointer handling here
 * kept losing the tap to the scroll container.
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
    var buffer by remember { mutableStateOf(TextFieldValue("")) }
    var gainedFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current

    val current by rememberUpdatedState(value)
    val callback by rememberUpdatedState(onValueChange)

    fun commit() {
        val parsed = buffer.text.toIntOrNull()
        DebugLog.trace("keypad", "number commit buffer='${buffer.text}' parsed=$parsed")
        parsed?.let { callback(it.coerceIn(min, max)) }
        editing = false
        keyboard?.hide()
    }

    if (editing) {
        BasicTextField(
            value = buffer,
            onValueChange = { raw ->
                val clean = raw.text.filter { it.isDigit() || it == '-' }
                buffer = if (clean == raw.text) raw
                else TextFieldValue(clean, TextRange(clean.length))
            },
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
            keyboardActions = KeyboardActions(onDone = {
                commit()
                focus.clearFocus()
            }),
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    DebugLog.trace(
                        "keypad",
                        "number focus isFocused=${state.isFocused} hasFocus=${state.hasFocus} gainedBefore=$gainedFocus"
                    )
                    if (state.isFocused) {
                        gainedFocus = true
                        keyboard?.show()
                    } else if (gainedFocus && editing) {
                        commit()
                    }
                }
        )
        LaunchedEffect(Unit) {
            DebugLog.trace("keypad", "number field composed, controller=${if (keyboard == null) "NULL" else "ok"}")
            withFrameNanos { }
            runCatching { focusRequester.requestFocus() }
                .onSuccess { DebugLog.trace("keypad", "number requestFocus sent") }
                .onFailure { DebugLog.log("keypad", "number requestFocus FAILED: $it") }
            keyboard?.show()
        }
    } else {
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = modifier.clickable {
                DebugLog.trace("keypad", "number tapped, value=$current -> opening editor")
                val start = current.toString()
                // Caret at the end, not the start -- typing should extend the
                // number the way it reads, not prepend to it.
                buffer = TextFieldValue(start, TextRange(start.length))
                gainedFocus = false
                editing = true
            }
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
