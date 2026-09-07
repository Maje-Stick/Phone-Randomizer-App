package com.majestick.randomizer

import java.time.LocalDate
import kotlin.math.ln
import kotlin.math.max

/**
 * All randomization logic as pure functions. Every draw goes through [Rng],
 * so the entropy source is swappable in exactly one place.
 */

/* ----------------------------------------------------------------- list --- */

/** One row of the list tool: some text, and how heavily it is favoured. */
data class Entry(val text: String, val weight: Double = 1.0)

private const val FIELD_SEP = "\u0001"

fun encodeEntries(entries: List<Entry>): String =
    entries.joinToString("\n") { "${it.weight}$FIELD_SEP${it.text}" }

fun decodeEntries(raw: String): List<Entry> =
    raw.split("\n").mapNotNull { line ->
        if (line.isBlank()) return@mapNotNull null
        val cut = line.indexOf(FIELD_SEP)
        if (cut < 0) Entry(line.trim())
        else Entry(
            text = line.substring(cut + 1),
            weight = line.substring(0, cut).toDoubleOrNull()?.coerceAtLeast(0.0) ?: 1.0
        )
    }

/** Bulk entry: one item per line, all weighted equally. */
fun parseEntriesFromText(raw: String): List<Entry> =
    raw.split('\n', ',', ';')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { Entry(it) }

private fun weightedIndex(pool: List<Entry>): Int {
    val total = pool.sumOf { it.weight }
    if (total <= 0.0) return Rng.int(pool.size)
    var roll = Rng.double() * total
    pool.forEachIndexed { index, entry ->
        roll -= entry.weight
        if (roll <= 0.0) return index
    }
    return pool.lastIndex
}

/**
 * Produces an ordered result. Weight raises an entry's chance of taking the top
 * slot; once taken, the same draw repeats for the next slot from what is left.
 *
 * With repeats allowed each slot is drawn independently, so weights apply
 * unchanged at every position. Without repeats a full-length draw is always a
 * permutation of the input.
 */
fun weightedOrder(entries: List<Entry>, count: Int, allowRepeats: Boolean): List<String> {
    val usable = entries.filter { it.text.isNotBlank() }
    if (usable.isEmpty()) return emptyList()
    val n = count.coerceIn(1, 500)

    if (allowRepeats) return List(n) { usable[weightedIndex(usable)].text }

    val pool = usable.toMutableList()
    val out = ArrayList<String>(minOf(n, pool.size))
    repeat(minOf(n, pool.size)) {
        val index = weightedIndex(pool)
        out.add(pool[index].text)
        pool.removeAt(index)
    }
    return out
}

/* ------------------------------------------------------------- the rest --- */

fun flipCoins(count: Int): List<String> =
    List(count.coerceIn(1, 200)) { if (Rng.bool()) "Heads" else "Tails" }

fun rollDice(count: Int, sides: Int): List<Int> {
    val c = count.coerceIn(1, 100)
    val s = sides.coerceIn(2, 1000)
    return List(c) { Rng.int(s) + 1 }
}

fun randomNumbers(min: Int, max: Int, count: Int, unique: Boolean): List<Int> {
    val lo = minOf(min, max)
    val hi = maxOf(min, max)
    val n = count.coerceIn(1, 500)
    val span = hi.toLong() - lo.toLong() + 1L

    if (!unique) return List(n) { Rng.intInRange(lo, hi) }
    if (n >= span) return Rng.shuffled((lo..hi).toList())

    val picked = LinkedHashSet<Int>()
    while (picked.size < n) picked.add(Rng.intInRange(lo, hi))
    return picked.toList()
}

fun splitIntoTeams(items: List<String>, teams: Int): List<List<String>> {
    val t = teams.coerceIn(1, 50)
    val buckets = List(t) { mutableListOf<String>() }
    Rng.shuffled(items).forEachIndexed { i, item -> buckets[i % t].add(item) }
    return buckets
}

private const val UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ"
private const val LOWERCASE = "abcdefghijkmnopqrstuvwxyz"
private const val DIGITS = "23456789"
private const val SYMBOLS = "!@#\$%^&*_-+=?"

/**
 * Skips characters that are easy to misread (I, l, 1, O, 0) and guarantees at
 * least one character from every enabled set.
 */
fun generatePassword(
    length: Int,
    upper: Boolean,
    lower: Boolean,
    digits: Boolean,
    symbols: Boolean
): String {
    val pools = buildList {
        if (upper) add(UPPERCASE)
        if (lower) add(LOWERCASE)
        if (digits) add(DIGITS)
        if (symbols) add(SYMBOLS)
    }
    if (pools.isEmpty()) return ""

    val len = length.coerceIn(4, 128)
    val everything = pools.joinToString("")
    val chars = MutableList(len) { Rng.pickChar(everything) }

    pools.forEachIndexed { i, pool -> if (i < len) chars[i] = Rng.pickChar(pool) }
    return Rng.shuffled(chars).joinToString("")
}

private val SUITS = listOf("\u2660", "\u2665", "\u2666", "\u2663")
private val RANKS = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")

fun fullDeck(): List<String> = SUITS.flatMap { suit -> RANKS.map { rank -> "$rank$suit" } }

fun drawCards(count: Int): List<String> = Rng.shuffled(fullDeck()).take(count.coerceIn(1, 52))

/** Returns a packed 0xRRGGBB value. */
fun randomColor(): Int = Rng.int(0x1000000)

fun toHex(rgb: Int): String = "#%06X".format(rgb)

fun randomDate(start: LocalDate, end: LocalDate): LocalDate =
    LocalDate.ofEpochDay(Rng.longInRange(start.toEpochDay(), end.toEpochDay()))

fun randomTimeOfDay(): String {
    val minutes = Rng.int(24 * 60)
    return "%02d:%02d".format(minutes / 60, minutes % 60)
}

/* --------------------------------------------------------- distribution --- */

/**
 * The one tool that produces values which are not uniformly likely. A list can
 * fake any selection, but it cannot produce a bell curve.
 */
enum class Distribution(val label: String, val firstLabel: String, val secondLabel: String?) {
    UNIFORM("Uniform", "Lowest", "Highest"),
    NORMAL("Normal", "Mean", "Std deviation"),
    EXPONENTIAL("Exponential", "Mean", null)
}

fun sampleDistribution(
    kind: Distribution,
    first: Double,
    second: Double,
    count: Int
): List<Double> {
    val n = count.coerceIn(1, 5000)
    return when (kind) {
        Distribution.UNIFORM -> {
            val lo = minOf(first, second)
            val hi = maxOf(first, second)
            List(n) { lo + Rng.double() * (hi - lo) }
        }
        Distribution.NORMAL -> {
            val sd = max(second, 0.0)
            List(n) { first + Rng.gaussian() * sd }
        }
        Distribution.EXPONENTIAL -> {
            val mean = max(first, 1e-9)
            List(n) {
                var u = Rng.double()
                while (u <= 0.0) u = Rng.double()
                -mean * ln(u)
            }
        }
    }
}

/** Bucket counts for the histogram, plus the range they span. */
data class Histogram(val counts: List<Int>, val low: Double, val high: Double)

fun histogram(values: List<Double>, bins: Int = 14): Histogram {
    if (values.isEmpty()) return Histogram(emptyList(), 0.0, 0.0)
    val low = values.min()
    val high = values.max()
    if (high - low < 1e-12) return Histogram(List(bins) { if (it == bins / 2) values.size else 0 }, low, high)

    val counts = IntArray(bins)
    values.forEach { v ->
        val slot = (((v - low) / (high - low)) * bins).toInt().coerceIn(0, bins - 1)
        counts[slot]++
    }
    return Histogram(counts.toList(), low, high)
}
