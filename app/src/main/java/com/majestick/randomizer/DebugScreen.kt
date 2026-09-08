package com.majestick.randomizer

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val TAIL_SIZE = 200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var entries by remember { mutableStateOf(DebugLog.linesSnapshot()) }
    var knownSize by remember { mutableStateOf(-1) }
    var paused by remember { mutableStateOf(false) }
    var verbose by remember { mutableStateOf(DebugLog.verbose) }
    var hidden by remember { mutableStateOf(setOf<String>()) }
    var showReport by remember { mutableStateOf(false) }
    var confirmCrash by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }

    // Only rebuilds when the buffer actually changed, so a quiet log costs
    // nothing and a busy one does not redraw the whole list on a timer.
    LaunchedEffect(paused) {
        DebugLog.log("nav", if (paused) "debug view paused" else "debug view live")
        while (!paused) {
            val size = DebugLog.size()
            if (size != knownSize) {
                knownSize = size
                entries = DebugLog.linesSnapshot()
            }
            delay(400)
        }
    }

    val report = DebugLog.lastReport
    val visible = remember(entries, hidden) {
        entries.filter { it.tag !in hidden }.asReversed()
    }
    val tags = remember(entries) { entries.map { it.tag }.distinct().sorted() }

    fun copy(label: String, text: String) {
        clipboard.setText(AnnotatedString(text))
        val lines = if (text.isEmpty()) 0 else text.count { it == '\n' } + 1
        notice = "$label copied: $lines lines, ${text.length} characters"
    }

    fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Randomizer log")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        runCatching { context.startActivity(Intent.createChooser(send, "Share log")) }
            .onFailure { notice = "Share failed: ${it.message}. Use Copy instead." }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Debug log", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { share(DebugLog.fullReport()) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share whole log")
                    }
                    IconButton(onClick = {
                        DebugLog.clear()
                        knownSize = -1
                        entries = DebugLog.linesSnapshot()
                        notice = null
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear log")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                DebugLog.header().trim(),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))

            if (report != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "A crash or freeze was recorded",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        report.lineSequence().firstOrNull().orEmpty(),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { copy("Report", report) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("Copy report") }
                        OutlinedButton(
                            onClick = { showReport = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("View") }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // The intended workflow, in the order you use it.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val n = DebugLog.mark()
                        notice = "Mark $n dropped. Reproduce it, then Copy since mark."
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Mark") }
                OutlinedButton(
                    onClick = { copy("Since mark", DebugLog.textSinceMark()) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.4f)
                ) { Text("Copy since mark") }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { copy("Last $TAIL_SIZE", DebugLog.tail(TAIL_SIZE)) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Copy last $TAIL_SIZE") }
                OutlinedButton(
                    onClick = { copy("Whole log", DebugLog.fullReport()) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Copy all") }
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { paused = !paused },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text(if (paused) "Paused" else "Live") }
                OutlinedButton(
                    onClick = {
                        verbose = !verbose
                        DebugLog.verbose = verbose
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text(if (verbose) "Trace: on" else "Trace: off") }
                OutlinedButton(
                    onClick = { confirmCrash = true },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("Test crash") }
            }

            notice?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        val on = tag !in hidden
                        Text(
                            tag,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (on) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    if (on) MaterialTheme.colorScheme.surfaceVariant
                                    else MaterialTheme.colorScheme.background,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (on) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    hidden = if (on) hidden + tag else hidden - tag
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "Newest first. Filters affect this view only, not what Copy sends. "
                    + "The +ms column is the gap since the line above it.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(visible) { line ->
                    Text(
                        line.format(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = when (line.tag) {
                            "CRASH", "FREEZE" -> MaterialTheme.colorScheme.error
                            DebugLog.MARK_TAG -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }

    if (showReport && report != null) {
        AlertDialog(
            onDismissRequest = { showReport = false },
            title = { Text("Crash / freeze report") },
            text = {
                LazyColumn(modifier = Modifier.height(360.dp)) {
                    items(report.lines()) { line ->
                        Text(
                            line,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { copy("Report", report); showReport = false }) {
                    Text("Copy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReport = false }) { Text("Close") }
            }
        )
    }

    if (confirmCrash) {
        AlertDialog(
            onDismissRequest = { confirmCrash = false },
            title = { Text("Crash the app on purpose?") },
            text = {
                Text(
                    "This proves the crash catcher works. The app will close "
                        + "immediately. Reopen it, come back here, and the report "
                        + "should be waiting at the top of this screen."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    DebugLog.log("debug", "deliberate test crash requested")
                    throw IllegalStateException("Deliberate test crash from the Debug screen")
                }) { Text("Crash it") }
            },
            dismissButton = {
                TextButton(onClick = { confirmCrash = false }) { Text("Cancel") }
            }
        )
    }
}
