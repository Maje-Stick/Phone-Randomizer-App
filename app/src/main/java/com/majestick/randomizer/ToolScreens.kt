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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

@Composable
fun CoinScreen(onBack: () -> Unit) {
    var count by draft("coin.count", 1)
    var flips by draft<List<String>?>("coin.result", null)

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
    var count by draft("dice.count", 2)
    var sides by draft("dice.sides", 6)
    var rolls by draft<List<Int>?>("dice.result", null)
    var notation by draft("dice.notation", "")

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
    var min by draft("number.min", "1")
    var max by draft("number.max", "100")
    var count by draft("number.count", 1)
    var unique by draft("number.unique", true)
    var numbers by draft<List<Int>?>("number.result", null)
    var caption by draft("number.caption", "")

    ToolScaffold("Number range", onBack, "Generate", {
        val lo = min.toDoubleOrNull()?.toInt() ?: 1
        val hi = max.toDoubleOrNull()?.toInt() ?: 100
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

/**
 * Absorbs what used to be three tools. A plain list is every weight at 1; a
 * shuffle is a full-length draw with repeats off.
 */
@Composable
fun ListScreen(onBack: () -> Unit) {
    var entries by draft("list.entries", listOf(Entry(""), Entry("")))
    var count by draft("list.count", 1)
    var repeats by draft("list.repeats", false)
    var result by draft<List<String>?>("list.result", null)
    var caption by draft("list.caption", "")

    ToolScaffold("List", onBack, "Draw", {
        val usable = entries.filter { it.text.isNotBlank() }
        if (usable.isEmpty()) {
            result = null
            caption = ""
        } else {
            result = weightedOrder(usable, count, repeats)
            val weighted = usable.any { it.weight != 1.0 }
            caption = "from ${usable.size} entries" + if (weighted) ", weighted" else ""
        }
    }) {
        ListResult(result, caption, "Add entries below, then draw.")
        SectionSpacer()
        PresetBar(
            toolId = "list",
            capture = {
                mapOf(
                    "entries" to encodeEntries(entries),
                    "count" to count.toString(),
                    "repeats" to repeats.toString()
                )
            },
            apply = {
                entries = decodeEntries(it.str("entries", encodeEntries(entries)))
                count = it.int("count", count)
                repeats = it.bool("repeats", repeats)
            }
        )
        SectionSpacer()
        EntryListEditor(entries) { entries = it }
        Spacer(Modifier.height(14.dp))
        Stepper("How many to draw", count, { count = it }, min = 1, max = 200)
        ToggleRow("Allow repeats", repeats) { repeats = it }
        Spacer(Modifier.height(10.dp))
        Text(
            "Weight raises the odds of landing at the top. Draw as many as you have "
                + "entries with repeats off to get a full weighted shuffle.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TeamsScreen(onBack: () -> Unit) {
    var raw by draft("teams.raw", "")
    var teams by draft("teams.count", 2)
    var result by draft<List<List<String>>?>("teams.result", null)

    ToolScaffold("Split into teams", onBack, "Split", {
        val names = parseEntriesFromText(raw).map { it.text }
        result = if (names.isEmpty()) null else splitIntoTeams(names, teams)
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
    var length by draft("pw.length", 16)
    var upper by draft("pw.upper", true)
    var lower by draft("pw.lower", true)
    var digits by draft("pw.digits", true)
    var symbols by draft("pw.symbols", true)
    var password by draft<String?>("pw.result", null)

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
    var count by draft("cards.count", 1)
    var cards by draft<List<String>?>("cards.result", null)

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
    var rgb by draft<Int?>("color.result", null)

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
fun DateScreen(onBack: () -> Unit) {
    val today = LocalDate.now()
    var start by draft("date.start", today.toString())
    var end by draft("date.end", today.plusYears(1).toString())
    var withTime by draft("date.withTime", false)
    var drawn by draft<LocalDate?>("date.result", null)
    var drawnTime by draft<String?>("date.resultTime", null)

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

/**
 * The only tool whose outcomes are not uniformly likely. A list can imitate any
 * selection; it cannot produce a bell curve.
 */
@Composable
fun DistributionScreen(onBack: () -> Unit) {
    var kind by draft("dist.kind", Distribution.NORMAL)
    var first by draft("dist.first", "100")
    var second by draft("dist.second", "15")
    var count by draft("dist.count", 200)
    var values by draft<List<Double>?>("dist.result", null)
    var bars by draft<Histogram?>("dist.hist", null)
    var caption by draft("dist.caption", "")

    ToolScaffold("Distribution", onBack, "Sample", {
        val a = first.toDoubleOrNull() ?: 0.0
        val b = second.toDoubleOrNull() ?: 1.0
        val drawn = sampleDistribution(kind, a, b, count)
        values = drawn
        bars = histogram(drawn)
        caption = "mean of $count ${kind.label.lowercase()} samples"
    }) {
        DistributionResult(
            values,
            bars,
            caption,
            "Pick a shape and sample it. The histogram shows where values actually landed."
        )
        SectionSpacer()
        PresetBar(
            toolId = "distribution",
            capture = {
                mapOf(
                    "kind" to kind.name,
                    "first" to first,
                    "second" to second,
                    "count" to count.toString()
                )
            },
            apply = { stored ->
                kind = runCatching { Distribution.valueOf(stored.str("kind", kind.name)) }
                    .getOrDefault(kind)
                first = stored.str("first", first)
                second = stored.str("second", second)
                count = stored.int("count", count)
            }
        )
        SectionSpacer()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Distribution.values().forEach { option ->
                FilterChip(
                    selected = kind == option,
                    onClick = { kind = option },
                    label = { Text(option.label) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberInput(kind.firstLabel, first, { first = it }, Modifier.weight(1f))
            if (kind.secondLabel != null) {
                NumberInput(kind.secondLabel!!, second, { second = it }, Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
        Stepper("Samples", count, { count = it }, min = 10, max = 5000, step = 10)
        Spacer(Modifier.height(10.dp))
        Text(
            "Normal clusters around the mean and thins out at the edges. Exponential "
                + "favours small values with a long tail. Uniform treats every value alike.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
