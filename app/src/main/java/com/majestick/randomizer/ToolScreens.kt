package com.majestick.randomizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

@Composable
fun CoinScreen(onBack: () -> Unit) {
    var count by rememberSaveable { mutableStateOf(1) }
    var flips by remember { mutableStateOf<List<String>?>(null) }

    ToolScaffold("Coin flip", onBack, "Flip", { flips = flipCoins(count) }) {
        CoinResult(flips, "Flip once, or flip a hundred times and see the split.")
        SectionSpacer()
        PresetBar(
            toolId = "coin",
            capture = { mapOf("count" to count.toString()) },
            apply = { count = it.int("count", count) }
        )
        SectionSpacer()
        Stepper("Coins", count, { count = it }, min = 1, max = 200)
    }
}

@Composable
fun DiceScreen(onBack: () -> Unit) {
    var count by rememberSaveable { mutableStateOf(2) }
    var sides by rememberSaveable { mutableStateOf(6) }
    var rolls by remember { mutableStateOf<List<Int>?>(null) }
    var notation by remember { mutableStateOf("") }

    ToolScaffold("Dice", onBack, "Roll ${count}d$sides", {
        rolls = rollDice(count, sides)
        notation = "${count}d$sides"
    }) {
        DiceResult(rolls, notation, "Pick a die and how many. The total lands here.")
        SectionSpacer()
        PresetBar(
            toolId = "dice",
            capture = { mapOf("count" to count.toString(), "sides" to sides.toString()) },
            apply = {
                count = it.int("count", count)
                sides = it.int("sides", sides)
            }
        )
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
    var numbers by remember { mutableStateOf<List<Int>?>(null) }
    var caption by remember { mutableStateOf("") }

    ToolScaffold("Number range", onBack, "Generate", {
        val lo = min.toIntOrNull() ?: 1
        val hi = max.toIntOrNull() ?: 100
        val drawn = randomNumbers(lo, hi, count, unique)
        numbers = drawn
        caption = if (drawn.size == 1) "between $lo and $hi"
        else "${drawn.size} numbers between $lo and $hi"
    }) {
        NumbersResult(numbers, caption, "Set a range. Draw one number or a whole batch.")
        SectionSpacer()
        PresetBar(
            toolId = "number",
            capture = {
                mapOf(
                    "min" to min,
                    "max" to max,
                    "count" to count.toString(),
                    "unique" to unique.toString()
                )
            },
            apply = {
                min = it.str("min", min)
                max = it.str("max", max)
                count = it.int("count", count)
                unique = it.bool("unique", unique)
            }
        )
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
    var picked by remember { mutableStateOf<List<String>?>(null) }
    var caption by remember { mutableStateOf("") }

    ToolScaffold("Pick from a list", onBack, "Pick", {
        val items = parseItems(raw)
        if (items.isEmpty()) {
            picked = null
            caption = ""
        } else {
            picked = pickItems(items, count, unique)
            caption = "from ${items.size} items"
        }
    }) {
        PickResult(picked, caption, "Add your options below, then pick.")
        SectionSpacer()
        PresetBar(
            toolId = "pick",
            capture = {
                mapOf(
                    "raw" to raw,
                    "count" to count.toString(),
                    "unique" to unique.toString()
                )
            },
            apply = {
                raw = it.str("raw", raw)
                count = it.int("count", count)
                unique = it.bool("unique", unique)
            }
        )
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
    var order by remember { mutableStateOf<List<String>?>(null) }

    ToolScaffold("Shuffle order", onBack, "Shuffle", {
        val items = parseItems(raw)
        order = if (items.isEmpty()) null else Rng.shuffled(items)
    }) {
        ShuffleResult(order, "Put a list in, get it back in a new order.")
        SectionSpacer()
        PresetBar(
            toolId = "shuffle",
            capture = { mapOf("raw" to raw) },
            apply = { raw = it.str("raw", raw) }
        )
        SectionSpacer()
        ItemsInput(raw, { raw = it })
    }
}

@Composable
fun TeamsScreen(onBack: () -> Unit) {
    var raw by rememberSaveable { mutableStateOf("") }
    var teams by rememberSaveable { mutableStateOf(2) }
    var result by remember { mutableStateOf<List<List<String>>?>(null) }

    ToolScaffold("Split into teams", onBack, "Split", {
        val items = parseItems(raw)
        result = if (items.isEmpty()) null else splitIntoTeams(items, teams)
    }) {
        TeamsResult(result, "Names go in, balanced teams come out.")
        SectionSpacer()
        PresetBar(
            toolId = "teams",
            capture = { mapOf("raw" to raw, "teams" to teams.toString()) },
            apply = {
                raw = it.str("raw", raw)
                teams = it.int("teams", teams)
            }
        )
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
    var password by remember { mutableStateOf<String?>(null) }

    ToolScaffold("Password", onBack, "Generate", {
        password = generatePassword(length, upper, lower, digits, symbols).ifEmpty { null }
    }) {
        PasswordResult(password, "Turn on at least one character set, then generate.")
        SectionSpacer()
        PresetBar(
            toolId = "password",
            capture = {
                mapOf(
                    "length" to length.toString(),
                    "upper" to upper.toString(),
                    "lower" to lower.toString(),
                    "digits" to digits.toString(),
                    "symbols" to symbols.toString()
                )
            },
            apply = {
                length = it.int("length", length)
                upper = it.bool("upper", upper)
                lower = it.bool("lower", lower)
                digits = it.bool("digits", digits)
                symbols = it.bool("symbols", symbols)
            }
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
    var cards by remember { mutableStateOf<List<String>?>(null) }

    ToolScaffold("Draw cards", onBack, "Draw", { cards = drawCards(count) }) {
        CardsResult(cards, "Draw from a freshly shuffled deck.")
        SectionSpacer()
        PresetBar(
            toolId = "cards",
            capture = { mapOf("count" to count.toString()) },
            apply = { count = it.int("count", count) }
        )
        SectionSpacer()
        Stepper("Cards", count, { count = it }, min = 1, max = 52)
    }
}

@Composable
fun ColorScreen(onBack: () -> Unit) {
    var rgb by rememberSaveable { mutableStateOf<Int?>(null) }

    ToolScaffold("Random color", onBack, "Generate", { rgb = randomColor() }) {
        ColorResult(rgb, "Generate a color and its hex code lands here.")
        SectionSpacer()
        Text(
            "Every channel is drawn independently, so all 16.7 million values are equally likely.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeightedScreen(onBack: () -> Unit) {
    var raw by rememberSaveable { mutableStateOf("") }
    var winner by remember { mutableStateOf<String?>(null) }
    var share by remember { mutableStateOf<Double?>(null) }

    ToolScaffold("Weighted pick", onBack, "Pick", {
        val entries = parseWeighted(raw)
        val picked = weightedPick(entries)
        winner = picked
        share = picked?.let { name ->
            val total = entries.sumOf { it.second }
            entries.first { it.first == name }.second / total
        }
    }) {
        WeightedResult(winner, share, "Give each option a weight. Bigger weight, better odds.")
        SectionSpacer()
        PresetBar(
            toolId = "weighted",
            capture = { mapOf("raw" to raw) },
            apply = { raw = it.str("raw", raw) }
        )
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
    var drawn by remember { mutableStateOf<LocalDate?>(null) }
    var drawnTime by remember { mutableStateOf<String?>(null) }

    ToolScaffold("Random date", onBack, "Generate", {
        val a = runCatching { LocalDate.parse(start, DATE_FORMAT) }.getOrNull()
        val b = runCatching { LocalDate.parse(end, DATE_FORMAT) }.getOrNull()
        if (a == null || b == null) {
            drawn = null
            drawnTime = null
        } else {
            drawn = randomDate(a, b)
            drawnTime = if (withTime) randomTimeOfDay() else null
        }
    }) {
        DateResult(drawn, drawnTime, "Pick a window and draw a date from inside it.")
        SectionSpacer()
        PresetBar(
            toolId = "date",
            capture = {
                mapOf("start" to start, "end" to end, "withTime" to withTime.toString())
            },
            apply = {
                start = it.str("start", start)
                end = it.str("end", end)
                withTime = it.bool("withTime", withTime)
            }
        )
        SectionSpacer()
        NumberInput("From (YYYY-MM-DD)", start, { start = it }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        NumberInput("To (YYYY-MM-DD)", end, { end = it }, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        ToggleRow("Add a time of day", withTime) { withTime = it }
    }
}

@Composable
fun LetterScreen(onBack: () -> Unit) {
    var count by rememberSaveable { mutableStateOf(1) }
    var letters by remember { mutableStateOf<List<Char>?>(null) }

    ToolScaffold("Random letters", onBack, "Generate", {
        letters = randomLetters(count).filter { it.isLetter() }.toList()
    }) {
        LettersResult(letters, "Handy for word games and quick labels.")
        SectionSpacer()
        PresetBar(
            toolId = "letter",
            capture = { mapOf("count" to count.toString()) },
            apply = { count = it.int("count", count) }
        )
        SectionSpacer()
        Stepper("Letters", count, { count = it }, min = 1, max = 100)
    }
}
