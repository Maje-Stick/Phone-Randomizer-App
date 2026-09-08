package com.majestick.randomizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class HelpEntry(
    val icon: ImageVector,
    val title: String,
    val body: String
)

private val HELP = listOf(
    HelpEntry(
        Icons.Filled.Layers,
        "Weights, in the List tool",
        "Every entry has a weight next to it, starting at 1. Raise one and it "
            + "comes up more often. An entry at 3 is three times as likely as an "
            + "entry at 1. Set a weight to 0 and it is skipped entirely without "
            + "you having to delete it."
    ),
    HelpEntry(
        Icons.Filled.TouchApp,
        "Faster number changes",
        "Any plus or minus button repeats if you hold it down, so you do not "
            + "have to tap forty times. You can also tap the number itself and "
            + "type a value straight in."
    ),
    HelpEntry(
        Icons.Filled.ContentCopy,
        "Text view",
        "The List tool has a text view that shows every entry as plain lines. "
            + "Paste a whole list in at once, reorder it, rename things, then "
            + "Apply. Weights follow their row, so renaming and reordering both "
            + "keep them."
    ),
    HelpEntry(
        Icons.Filled.DragIndicator,
        "Rearranging the home screen",
        "Hold any card on the home screen and drag it where you want it. The "
            + "order is remembered. Settings has a reset if you want the "
            + "original layout back."
    ),
    HelpEntry(
        Icons.Filled.Save,
        "Presets",
        "Most tools let you save their current setup as a preset and load it "
            + "later. Useful for a list of names you use every week, or a dice "
            + "setup for a particular game."
    ),
    HelpEntry(
        Icons.Filled.Palette,
        "Themes",
        "Settings has plain palettes and four scenic ones. Nature, Sea, Space "
            + "and Night City each paint a gradient behind the whole app."
    ),
    HelpEntry(
        Icons.Filled.Casino,
        "About the randomness",
        "Results come from the operating system's cryptographic random source, "
            + "and ranges are generated without modulo bias. A one-in-six roll "
            + "really is one in six."
    ),
    HelpEntry(
        Icons.Filled.Lock,
        "Privacy",
        "The app asks for no permissions and has no internet access. Nothing "
            + "you type goes anywhere. The debug log in Settings stays on your "
            + "phone unless you deliberately copy or share it."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("How it works", style = MaterialTheme.typography.titleMedium) },
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
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Pick a tool, press the big button, get an answer. Everything "
                    + "below is the parts that are not obvious.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))

            HELP.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        entry.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            entry.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            entry.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
