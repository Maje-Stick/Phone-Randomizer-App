package com.majestick.randomizer

import java.time.LocalDate

/**
 * All randomization logic as pure functions. Every draw goes through [Rng],
 * so the entropy source is swappable in exactly one place.
 */

/** Splits pasted text on newlines, commas and semicolons. */
fun parseItems(raw: String): List<String> =
    raw.split('\n', ',', ';')
        .map { it.trim() }
        .filter { it.isNotEmpty() }

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

fun pickItems(items: List<String>, count: Int, unique: Boolean): List<String> {
    if (items.isEmpty()) return emptyList()
    val n = count.coerceIn(1, 500)
    return if (unique) Rng.shuffled(items).take(n)
    else List(n) { Rng.pick(items) }
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

/**
 * Accepts one entry per line, optionally `item : weight`.
 * A missing or invalid weight defaults to 1.
 */
fun parseWeighted(raw: String): List<Pair<String, Double>> =
    raw.split('\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { line ->
            val cut = line.lastIndexOf(':')
            val weight = if (cut > 0) line.substring(cut + 1).trim().toDoubleOrNull() else null
            if (weight != null && weight > 0.0) line.substring(0, cut).trim() to weight
            else line to 1.0
        }
        .filter { it.first.isNotEmpty() }

fun weightedPick(entries: List<Pair<String, Double>>): String? {
    if (entries.isEmpty()) return null
    val total = entries.sumOf { it.second }
    var roll = Rng.double() * total
    for ((item, weight) in entries) {
        roll -= weight
        if (roll <= 0.0) return item
    }
    return entries.last().first
}

fun randomDate(start: LocalDate, end: LocalDate): LocalDate =
    LocalDate.ofEpochDay(Rng.longInRange(start.toEpochDay(), end.toEpochDay()))

fun randomTimeOfDay(): String {
    val minutes = Rng.int(24 * 60)
    return "%02d:%02d".format(minutes / 60, minutes % 60)
}

fun randomLetters(count: Int): String {
    val letters = ('A'..'Z').joinToString("")
    return (1..count.coerceIn(1, 100)).joinToString(" ") { Rng.pickChar(letters).toString() }
}
