package com.majestick.randomizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RandomizerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
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
    Tool("coin", "Coin flip", Icons.Filled.Paid),
    Tool("dice", "Dice", Icons.Filled.Casino),
    Tool("number", "Number range", Icons.Filled.Numbers),
    Tool("pick", "Pick from a list", Icons.Filled.TouchApp),
    Tool("shuffle", "Shuffle order", Icons.Filled.Shuffle),
    Tool("teams", "Split into teams", Icons.Filled.Groups),
    Tool("weighted", "Weighted pick", Icons.Filled.Balance),
    Tool("password", "Password", Icons.Filled.Password),
    Tool("cards", "Draw cards", Icons.Filled.Style),
    Tool("decide", "Yes or no", Icons.Filled.HelpOutline),
    Tool("color", "Random color", Icons.Filled.Palette),
    Tool("date", "Random date", Icons.Filled.Event),
    Tool("letter", "Random letters", Icons.Filled.SortByAlpha)
)

@Composable
fun App() {
    var openTool by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = openTool != null) { openTool = null }

    val back = { openTool = null }
    when (openTool) {
        null -> HomeScreen { openTool = it }
        "coin" -> CoinScreen(back)
        "dice" -> DiceScreen(back)
        "number" -> NumberScreen(back)
        "pick" -> PickScreen(back)
        "shuffle" -> ShuffleScreen(back)
        "teams" -> TeamsScreen(back)
        "weighted" -> WeightedScreen(back)
        "password" -> PasswordScreen(back)
        "cards" -> CardsScreen(back)
        "decide" -> DecideScreen(back)
        "color" -> ColorScreen(back)
        "date" -> DateScreen(back)
        "letter" -> LetterScreen(back)
        else -> HomeScreen { openTool = it }
    }
}

@Composable
private fun HomeScreen(onOpen: (String) -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column(Modifier.padding(top = 20.dp, bottom = 12.dp)) {
                    Text(
                        "Randomizer",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${TOOLS.size} ways to stop deciding.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(TOOLS, key = { it.id }) { tool ->
                ToolCard(tool) { onOpen(tool.id) }
            }
        }
    }
}

@Composable
private fun ToolCard(tool: Tool, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
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
