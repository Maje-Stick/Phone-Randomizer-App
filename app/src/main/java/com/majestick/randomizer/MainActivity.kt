package com.majestick.randomizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.distinctUntilChanged

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugLog.install(this)
        Sounds.init(this)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var themeId by rememberSaveable { mutableStateOf(ThemeStore.load(context)) }

            RandomizerTheme(themeId) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    App(
                        themeId = themeId,
                        onThemeChange = {
                            themeId = it
                            ThemeStore.save(context, it)
                        }
                    )
                }
            }
        }
    }
}

private data class Tool(
    val id: String,
    val name: String,
    val icon: ImageVector
)

private val TOOLS = listOf(
    Tool("list", "List", Icons.Filled.Layers),
    Tool("number", "Number range", Icons.Filled.Numbers),
    Tool("coin", "Coin flip", Icons.Filled.Paid),
    Tool("color", "Random color", Icons.Filled.Palette),
    Tool("teams", "Split into teams", Icons.Filled.Groups),
    Tool("dice", "Dice", Icons.Filled.Casino),
    Tool("distribution", "Distribution", Icons.Filled.ShowChart),
    Tool("password", "Password", Icons.Filled.Password),
    Tool("date", "Random date", Icons.Filled.Event),
    Tool("cards", "Draw cards", Icons.Filled.Style)
)

@Composable
fun App(themeId: String, onThemeChange: (String) -> Unit) {
    var openTool by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(openTool) {
        DebugLog.log("nav", "screen = ${openTool ?: "home"}")
    }

    // Whether the soft keyboard actually rose is otherwise invisible from inside
    // the app, and it is the whole question for the keypad bug.
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    LaunchedEffect(Unit) {
        snapshotFlow { imeInsets.getBottom(density) }
            .distinctUntilChanged()
            .collect { DebugLog.log("ime", if (it > 0) "keyboard visible, height ${it}px" else "keyboard hidden") }
    }

    LaunchedEffect(themeId) { Sounds.useTheme(themeId) }

    BackHandler(enabled = openTool != null) { openTool = null }

    val back = { openTool = null }
    val screen = when (openTool) {
        null -> "home"
        "settings", "help", "debug" -> openTool!!
        else -> "tool"
    }

    ThemeBackdrop(themeId = themeId, screen = screen) {
    when (openTool) {
        null -> HomeScreen(
            onOpen = { openTool = it },
            onSettings = { openTool = "settings" },
            onHelp = { openTool = "help" }
        )
        "help" -> HelpScreen(back)
        "settings" -> SettingsScreen(
            themeId = themeId,
            onThemeChange = onThemeChange,
            onDebug = { openTool = "debug" },
            onBack = back
        )
        "debug" -> DebugScreen(back)
        "coin" -> CoinScreen(back)
        "dice" -> DiceScreen(back)
        "number" -> NumberScreen(back)
        "list" -> ListScreen(back)
        "teams" -> TeamsScreen(back)
        "password" -> PasswordScreen(back)
        "cards" -> CardsScreen(back)
        "color" -> ColorScreen(back)
        "date" -> DateScreen(back)
        "distribution" -> DistributionScreen(back)
        else -> HomeScreen(
            onOpen = { openTool = it },
            onSettings = { openTool = "settings" },
            onHelp = { openTool = "help" }
        )
    }
    }
}

private const val HEADER_KEY = "home-header"

/** Whether a point inside the grid's own coordinate space lands on this item. */
private fun LazyGridItemInfo.holds(point: Offset): Boolean =
    point.x >= offset.x && point.x < offset.x + size.width &&
        point.y >= offset.y && point.y < offset.y + size.height

@Composable
private fun HomeScreen(
    onOpen: (String) -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val view = LocalView.current
    val gridState = rememberLazyGridState()

    val defaults = remember { TOOLS.map { it.id } }
    var order by remember { mutableStateOf(ToolOrderStore.load(context, defaults)) }
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragDelta by remember { mutableStateOf(Offset.Zero) }

    val ordered = remember(order) {
        order.mapNotNull { id -> TOOLS.firstOrNull { it.id == id } }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            // Handing the grid's own scrolling off while a card is held is what
            // keeps the two gestures from fighting. Without it the inner
            // scrollable claims the drag first and the grid scrolls instead.
            userScrollEnabled = draggingKey == null,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(padding)
                .pointerInput(Unit) {
                    // detectDragGesturesAfterLongPress is the platform's own
                    // arbitration: it claims nothing until the long press has
                    // already succeeded, so an ordinary flick still scrolls.
                    detectDragGesturesAfterLongPress(
                        onDragStart = { start ->
                            val hit = gridState.layoutInfo.visibleItemsInfo.firstOrNull {
                                it.key != HEADER_KEY && it.holds(start)
                            }
                            val key = hit?.key as? String
                            if (key != null) {
                                draggingKey = key
                                dragDelta = Offset.Zero
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                DebugLog.log("reorder", "picked up $key")
                            }
                        },
                        onDrag = { change, delta ->
                            val key = draggingKey
                            if (key != null) {
                                change.consume()
                                dragDelta += delta
                                val visible = gridState.layoutInfo.visibleItemsInfo
                                val dragged = visible.firstOrNull { it.key == key }
                                if (dragged != null) {
                                    val centre = Offset(
                                        dragged.offset.x + dragged.size.width / 2f + dragDelta.x,
                                        dragged.offset.y + dragged.size.height / 2f + dragDelta.y
                                    )
                                    val target = visible.firstOrNull {
                                        it.key != key && it.key != HEADER_KEY && it.holds(centre)
                                    }
                                    val to = (target?.key as? String)?.let { order.indexOf(it) } ?: -1
                                    val from = order.indexOf(key)
                                    if (target != null && from >= 0 && to >= 0 && from != to) {
                                        // The card is about to inherit the target's
                                        // slot, so cancel that jump out of the offset
                                        // and it stays put under the finger.
                                        dragDelta += Offset(
                                            (dragged.offset.x - target.offset.x).toFloat(),
                                            (dragged.offset.y - target.offset.y).toFloat()
                                        )
                                        order = order.toMutableList()
                                            .apply { add(to, removeAt(from)) }
                                        haptics.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove
                                        )
                                        DebugLog.trace("reorder") { "$key $from -> $to" }
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            if (draggingKey != null) {
                                ToolOrderStore.save(context, order)
                                DebugLog.log("reorder", "dropped, saved ${order.joinToString(",")}")
                            }
                            draggingKey = null
                            dragDelta = Offset.Zero
                        },
                        onDragCancel = {
                            DebugLog.log("reorder", "drag cancelled, order kept")
                            if (draggingKey != null) ToolOrderStore.save(context, order)
                            draggingKey = null
                            dragDelta = Offset.Zero
                        }
                    )
                }
        ) {
            item(key = HEADER_KEY, span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Sweet Simple",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Randomizer",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Hold a card to move it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onHelp) {
                        Icon(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "How it works",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(ordered, key = { it.id }) { tool ->
                val held = tool.id == draggingKey
                ToolCard(
                    tool = tool,
                    held = held,
                    modifier = Modifier
                        .zIndex(if (held) 1f else 0f)
                        .then(
                            if (held) Modifier.graphicsLayer {
                                translationX = dragDelta.x
                                translationY = dragDelta.y
                                scaleX = 1.04f
                                scaleY = 1.04f
                                shadowElevation = 16.dp.toPx()
                                shape = RoundedCornerShape(18.dp)
                                clip = false
                            } else Modifier.animateItem()
                        )
                ) {
                    Sounds.click(view)
                    onOpen(tool.id)
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: Tool,
    held: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (held) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(18.dp)
            )
            .clickable(enabled = !held, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            tool.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Text(
            tool.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ThemeRow(
    theme: AppTheme,
    selected: String,
    onPick: (String) -> Unit
) {
    val view = LocalView.current
    val isOn = theme.id == selected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(
                if (isOn) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (isOn) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .clickable {
                Sounds.click(view)
                DebugLog.log("settings", "theme -> ${theme.id}")
                onPick(theme.id)
            }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(theme.swatch, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            theme.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (isOn) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    themeId: String,
    onThemeChange: (String) -> Unit,
    onDebug: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var soundsOn by remember { mutableStateOf(Sounds.enabled) }
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Colour themes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Themes.plain.forEach { theme -> ThemeRow(theme, themeId, onThemeChange) }

            Spacer(Modifier.height(22.dp))
            Text(
                "Artistic themes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Each one paints artwork behind the app and brings its own "
                    + "tap and draw sounds.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Themes.artistic.forEach { theme -> ThemeRow(theme, themeId, onThemeChange) }

            Spacer(Modifier.height(24.dp))
            Text(
                "Sound",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable {
                        soundsOn = !soundsOn
                        Sounds.enabled = soundsOn
                    }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Interface sounds",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Taps follow your system sound setting. The draw button "
                            + "has its own chime.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = soundsOn, onCheckedChange = {
                    soundsOn = it
                    Sounds.enabled = it
                })
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Home grid",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    ToolOrderStore.clear(context)
                    DebugLog.log("settings", "tool order reset")
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Reset tool order") }
            Spacer(Modifier.height(6.dp))
            Text(
                "Puts the cards back in their original order. Takes effect when "
                    + "you go back to the home screen.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            Text(
                "Diagnostics",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clickable(onClick = onDebug)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.BugReport,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Debug log",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Activity log, plus any crash or freeze report.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
