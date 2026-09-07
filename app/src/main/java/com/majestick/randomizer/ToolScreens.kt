package com.majestick.randomizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

@Composable
fun CoinScreen(onBack: () -> Unit) {
    var count by rememberSaveable { mutableStateOf(1) }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Coin flip", onBack, "Flip", {
        val flips = flipCoins(count)
        if (flips.size == 1) {
            headline = flips.first()
            detail = null
        } else {
            val heads = flips.count { it == "Heads" }
            headline = "$heads / ${flips.size - heads}"
            detail = "Heads / Tails\n" + flips.joinToString(" ") { it.first().toString() }
        }
    }) {
        ResultBoard(headline, detail, "Flip once, or flip a hundred times and see the split.")
        SectionSpacer()
        Stepper("Coins", count, { count = it }, min = 1, max = 200)
    }
}

@Composable
fun DiceScreen(onBack: () -> Unit) {
    var count by rememberSaveable { mutableStateOf(2) }
    var sides by rememberSaveable { mutableStateOf(6) }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Dice", onBack, "Roll ${count}d$sides", {
        val rolls = rollDice(count, sides)
        headline = rolls.sum().toString()
        detail = if (rolls.size == 1) null else rolls.joinToString(" + ") + " = ${rolls.sum()}"
    }) {
        ResultBoard(headline, detail, "Pick a die and how many. The total lands here.")
        SectionSpacer()
        Text(
            "Die",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4, 6, 8, 10, 12, 20).forEach { s ->
                FilterChip(
                    selected = sides == s,
                    onClick = { sides = s },
                    label = { Text("d$s") }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Stepper("Sides", sides, { sides = it }, min = 2, max = 1000)
        Stepper("Dice", count, { count = it }, min = 1, max = 100)
    }
}

@Composable
fun NumberScreen(onBack: () -> Unit) {
    var min by rememberSaveable { mutableStateOf("1") }
    var max by rememberSaveable { mutableStateOf("100") }
    var count by rememberSaveable { mutableStateOf(1) }
    var unique by rememberSaveable { mutableStateOf(true) }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Number range", onBack, "Generate", {
        val lo = min.toIntOrNull() ?: 1
        val hi = max.toIntOrNull() ?: 100
        val nums = randomNumbers(lo, hi, count, unique)
        if (nums.size == 1) {
            headline = nums.first().toString()
            detail = "between $lo and $hi"
        } else {
            headline = nums.joinToString(", ")
            detail = "${nums.size} numbers between $lo and $hi"
        }
    }) {
        ResultBoard(headline, detail, "Set a range. Draw one number or a whole batch.")
        SectionSpacer()
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberInput("Lowest", min, { min = it }, Modifier.weight(1f))
            NumberInput("Highest", max, { max = it }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Stepper("How many", count, { count = it }, min = 1, max = 500)
        ToggleRow("No repeats", unique) { unique = it }
    }
}

@Composable
fun PickScreen(onBack: () -> Unit) {
    var raw by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableStateOf(1) }
    var unique by rememberSaveable { mutableStateOf(true) }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Pick from a list", onBack, "Pick", {
        val items = parseItems(raw)
        if (items.isEmpty()) {
            headline = null
            detail = null
        } else {
            val picked = pickItems(items, count, unique)
            headline = picked.joinToString(", ")
            detail = "from ${items.size} items"
        }
    }) {
        ResultBoard(headline, detail, "Add your options below, then pick.")
        SectionSpacer()
        ItemsInput(raw, { raw = it })
        Spacer(Modifier.height(8.dp))
        Stepper("How many", count, { count = it }, min = 1, max = 100)
        ToggleRow("No repeats", unique) { unique = it }
    }
}

@Composable
fun ShuffleScreen(onBack: () -> Unit) {
    var raw by rememberSaveable { mutableStateOf("") }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Shuffle order", onBack, "Shuffle", {
        val items = parseItems(raw)
        headline = if (items.isEmpty()) null
        else items.shuffled().mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")
    }) {
        ResultBoard(headline, null, "Put a list in, get it back in a new order.")
        SectionSpacer()
        ItemsInput(raw, { raw = it })
    }
}

@Composable
fun TeamsScreen(onBack: () -> Unit) {
    var raw by rememberSaveable { mutableStateOf("") }
    var teams by rememberSaveable { mutableStateOf(2) }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Split into teams", onBack, "Split", {
        val items = parseItems(raw)
        headline = if (items.isEmpty()) null
        else splitIntoTeams(items, teams)
            .mapIndexed { i, group -> "Team ${i + 1}\n" + group.joinToString("\n") { "  $it" } }
            .joinToString("\n\n")
    }) {
        ResultBoard(headline, null, "Names go in, balanced teams come out.")
        SectionSpacer()
        ItemsInput(raw, { raw = it }, label = "Names")
        Spacer(Modifier.height(8.dp))
        Stepper("Teams", teams, { teams = it }, min = 2, max = 50)
    }
}

@Composable
fun PasswordScreen(onBack: () -> Unit) {
    var length by rememberSaveable { mutableStateOf(16) }
    var upper by rememberSaveable { mutableStateOf(true) }
    var lower by rememberSaveable { mutableStateOf(true) }
    var digits by rememberSaveable { mutableStateOf(true) }
    var symbols by rememberSaveable { mutableStateOf(true) }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Password", onBack, "Generate", {
        val pw = generatePassword(length, upper, lower, digits, symbols)
        if (pw.isEmpty()) {
            headline = null
            detail = null
        } else {
            headline = pw
            detail = "${pw.length} characters. Tap the corner icon to copy."
        }
    }) {
        ResultBoard(
            headline,
            detail,
            "Turn on at least one character set, then generate."
        )
        SectionSpacer()
        Stepper("Length", length, { length = it }, min = 4, max = 128)
        ToggleRow("Uppercase", upper) { upper = it }
        ToggleRow("Lowercase", lower) { lower = it }
        ToggleRow("Numbers", digits) { digits = it }
        ToggleRow("Symbols", symbols) { symbols = it }
        Spacer(Modifier.height(10.dp))
        Text(
            "Look-alike characters (I, l, 1, O, 0) are left out so you can read it off the screen.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CardsScreen(onBack: () -> Unit) {
    var count by rememberSaveable { mutableStateOf(1) }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Draw cards", onBack, "Draw", {
        val cards = drawCards(count)
        headline = cards.joinToString("  ")
        detail = if (cards.size == 1) "from a shuffled 52-card deck"
        else "${cards.size} cards, no duplicates"
    }) {
        ResultBoard(headline, detail, "Draw from a freshly shuffled deck.")
        SectionSpacer()
        Stepper("Cards", count, { count = it }, min = 1, max = 52)
    }
}

@Composable
fun DecideScreen(onBack: () -> Unit) {
    var headline by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Yes or no", onBack, "Decide", { headline = decide() }) {
        ResultBoard(headline, null, "Ask your question out loud, then tap Decide.")
    }
}

@Composable
fun ColorScreen(onBack: () -> Unit) {
    var rgb by rememberSaveable { mutableStateOf<Int?>(null) }

    ToolScaffold("Random color", onBack, "Generate", { rgb = randomColor() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(
                    rgb?.let { Color(0xFF000000.toInt() or it) }
                        ?: MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(20.dp)
                )
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (rgb == null) {
                Text(
                    "Generate a color to fill this space.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        SectionSpacer()
        ResultBoard(
            rgb?.let { toHex(it) },
            rgb?.let {
                "R ${(it shr 16) and 0xFF}   G ${(it shr 8) and 0xFF}   B ${it and 0xFF}"
            },
            "The hex code will appear here."
        )
    }
}

@Composable
fun WeightedScreen(onBack: () -> Unit) {
    var raw by rememberSaveable { mutableStateOf("") }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Weighted pick", onBack, "Pick", {
        val entries = parseWeighted(raw)
        val winner = weightedPick(entries)
        headline = winner
        detail = winner?.let {
            val total = entries.sumOf { e -> e.second }
            val w = entries.first { e -> e.first == it }.second
            "%.1f%% chance".format(w / total * 100)
        }
    }) {
        ResultBoard(headline, detail, "Give each option a weight. Bigger weight, better odds.")
        SectionSpacer()
        ItemsInput(
            raw,
            { raw = it },
            label = "Options with weights",
            hint = "One per line, like:\nCommon : 70\nRare : 25\nLegendary : 5\nNo weight means 1."
        )
    }
}

@Composable
fun DateScreen(onBack: () -> Unit) {
    val today = LocalDate.now()
    var start by rememberSaveable { mutableStateOf(today.toString()) }
    var end by rememberSaveable { mutableStateOf(today.plusYears(1).toString()) }
    var withTime by rememberSaveable { mutableStateOf(false) }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Random date", onBack, "Generate", {
        val a = runCatching { LocalDate.parse(start, DATE_FORMAT) }.getOrNull()
        val b = runCatching { LocalDate.parse(end, DATE_FORMAT) }.getOrNull()
        if (a == null || b == null) {
            headline = null
            detail = null
        } else {
            val date = randomDate(a, b)
            headline = date.toString()
            detail = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } +
                if (withTime) " at ${randomTimeOfDay()}" else ""
        }
    }) {
        ResultBoard(headline, detail, "Pick a window and draw a date from inside it.")
        SectionSpacer()
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberInput("From (YYYY-MM-DD)", start, { start = it }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberInput("To (YYYY-MM-DD)", end, { end = it }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        ToggleRow("Add a time of day", withTime) { withTime = it }
    }
}

@Composable
fun LetterScreen(onBack: () -> Unit) {
    var count by rememberSaveable { mutableStateOf(1) }
    var headline by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold("Random letters", onBack, "Generate", {
        val letters = ('A'..'Z').toList()
        headline = (1..count.coerceIn(1, 100))
            .map { letters.random() }
            .joinToString(" ")
    }) {
        ResultBoard(headline, null, "Handy for word games and quick labels.")
        SectionSpacer()
        Stepper("Letters", count, { count = it }, min = 1, max = 100)
    }
}
