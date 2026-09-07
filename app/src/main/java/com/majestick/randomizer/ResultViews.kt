package com.majestick.randomizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

/**
 * Every tool shares this container so the app still reads as one thing, but
 * fills it with a layout suited to its own kind of answer.
 */
@Composable
fun ResultFrame(
    copyText: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clipboard = LocalClipboardManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 190.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
        content = {
            content()
            if (!copyText.isNullOrBlank()) {
                IconButton(
                    onClick = { clipboard.setText(AnnotatedString(copyText)) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = "Copy result",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun EmptyHint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Caption(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

/* ---------------------------------------------------------------- coin --- */

@Composable
private fun CoinDisc(isHeads: Boolean) {
    Box(
        modifier = Modifier
            .size(104.dp)
            .background(
                if (isHeads) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                CircleShape
            )
            .border(
                2.dp,
                if (isHeads) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (isHeads) "H" else "T",
            fontSize = 46.sp,
            fontWeight = FontWeight.Black,
            color = if (isHeads) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoinResult(flips: List<String>?, hint: String) {
    ResultFrame(copyText = flips?.joinToString(" ")) {
        when {
            flips == null -> EmptyHint(hint)

            flips.size == 1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CoinDisc(flips.first() == "Heads")
                Spacer(Modifier.height(14.dp))
                Text(
                    flips.first(),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            else -> {
                val heads = flips.count { it == "Heads" }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$heads",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "  /  ",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${flips.size - heads}",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Caption("heads / tails")
                    Spacer(Modifier.height(16.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        flips.take(80).forEach { flip ->
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .background(
                                        if (flip == "Heads") MaterialTheme.colorScheme.primary
                                        else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        CircleShape
                                    )
                            )
                        }
                    }
                    if (flips.size > 80) {
                        Spacer(Modifier.height(8.dp))
                        Caption("showing first 80")
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------------- dice --- */

@Composable
private fun Tile(text: String, accent: Boolean = false, wide: Boolean = false) {
    Box(
        modifier = Modifier
            .then(if (wide) Modifier.padding(horizontal = 4.dp) else Modifier)
            .size(if (wide) 58.dp else 46.dp)
            .background(
                if (accent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = if (text.length > 2) 15.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (accent) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiceResult(rolls: List<Int>?, notation: String, hint: String) {
    ResultFrame(copyText = rolls?.joinToString(", ")) {
        if (rolls == null) {
            EmptyHint(hint)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    rolls.sum().toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Caption(notation)
                if (rolls.size > 1) {
                    Spacer(Modifier.height(16.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rolls.take(40).forEach { Tile(it.toString()) }
                    }
                    if (rolls.size > 40) {
                        Spacer(Modifier.height(8.dp))
                        Caption("showing first 40 of ${rolls.size}")
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------- numbers --- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumbersResult(numbers: List<Int>?, caption: String, hint: String) {
    ResultFrame(copyText = numbers?.joinToString(", ")) {
        when {
            numbers == null -> EmptyHint(hint)

            numbers.size == 1 -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    numbers.first().toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Caption(caption)
            }

            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    numbers.take(60).forEach { Tile(it.toString(), wide = it.toString().length > 2) }
                }
                Spacer(Modifier.height(12.dp))
                Caption(caption)
            }
        }
    }
}

/* --------------------------------------------------------------- teams --- */

@Composable
fun TeamsResult(teams: List<List<String>>?, hint: String) {
    ResultFrame(
        copyText = teams?.mapIndexed { i, group ->
            "Team ${i + 1}\n" + group.joinToString("\n")
        }?.joinToString("\n\n")
    ) {
        if (teams.isNullOrEmpty()) {
            EmptyHint(hint)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                teams.forEachIndexed { index, group ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            "Team ${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(6.dp))
                        group.forEach { member ->
                            Text(
                                member,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------ password --- */

@Composable
fun PasswordResult(password: String?, hint: String) {
    ResultFrame(copyText = password) {
        if (password.isNullOrEmpty()) {
            EmptyHint(hint)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    password,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 32.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                Caption("${password.length} characters. Tap the corner icon to copy.")
            }
        }
    }
}

/* --------------------------------------------------------------- cards --- */

@Composable
private fun PlayingCard(card: String) {
    val isRed = card.endsWith("\u2665") || card.endsWith("\u2666")
    val rank = card.dropLast(1)
    val suit = card.takeLast(1)
    Box(
        modifier = Modifier
            .size(width = 62.dp, height = 86.dp)
            .background(Color(0xFFF7F5F0), RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                rank,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRed) Color(0xFFC0392B) else Color(0xFF1A1A1A)
            )
            Text(
                suit,
                fontSize = 22.sp,
                color = if (isRed) Color(0xFFC0392B) else Color(0xFF1A1A1A)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardsResult(cards: List<String>?, hint: String) {
    ResultFrame(copyText = cards?.joinToString(" ")) {
        if (cards.isNullOrEmpty()) {
            EmptyHint(hint)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cards.forEach { PlayingCard(it) }
                }
                if (cards.size > 1) {
                    Spacer(Modifier.height(12.dp))
                    Caption("${cards.size} cards, no duplicates")
                }
            }
        }
    }
}

/* ---------------------------------------------------------------- date --- */

@Composable
fun DateResult(date: LocalDate?, time: String?, hint: String) {
    ResultFrame(copyText = date?.let { "$it${time?.let { t -> " $t" } ?: ""}" }) {
        if (date == null) {
            EmptyHint(hint)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Caption(date.month.name.lowercase().replaceFirstChar { it.uppercase() })
                Text(
                    date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${date.year}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (time != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        time,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* --------------------------------------------------------------- color --- */

@Composable
fun ColorResult(rgb: Int?, hint: String) {
    ResultFrame(copyText = rgb?.let { toHex(it) }) {
        if (rgb == null) {
            EmptyHint(hint)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(width = 150.dp, height = 96.dp)
                        .background(Color(0xFF000000.toInt() or rgb), RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    toHex(rgb),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Caption("R ${(rgb shr 16) and 0xFF}   G ${(rgb shr 8) and 0xFF}   B ${rgb and 0xFF}")
            }
        }
    }
}

/* ---------------------------------------------------------------- list --- */

/**
 * Compact ordered output. Position carries the meaning here, so the numbers get
 * the accent colour and everything else stays out of the way.
 */
@Composable
fun ListResult(items: List<String>?, caption: String, hint: String) {
    ResultFrame(
        copyText = items?.mapIndexed { i, s -> "${i + 1}. $s" }?.joinToString("\n")
    ) {
        if (items.isNullOrEmpty()) {
            EmptyHint(hint)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (caption.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Caption(caption)
                }
            }
        }
    }
}

/* -------------------------------------------------------- distribution --- */

@Composable
fun DistributionResult(
    values: List<Double>?,
    bars: Histogram?,
    caption: String,
    hint: String
) {
    ResultFrame(copyText = values?.joinToString("\n") { trimNumber(it) }) {
        if (values.isNullOrEmpty() || bars == null || bars.counts.isEmpty()) {
            EmptyHint(hint)
        } else {
            val peak = (bars.counts.maxOrNull() ?: 1).coerceAtLeast(1)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    bars.counts.forEach { count ->
                        val fraction = (count.toFloat() / peak).coerceAtLeast(0.02f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(fraction)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Caption(trimNumber(bars.low))
                    Caption(trimNumber(bars.high))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    trimNumber(values.average()),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Caption(caption)
            }
        }
    }
}
