package com.majestick.randomizer

import java.security.SecureRandom

/**
 * The single source of randomness for the entire app.
 *
 * SecureRandom on Android is backed by the kernel CSPRNG, which is continuously
 * reseeded from hardware entropy (interrupt timing, and a hardware RNG on most
 * modern SoCs). Its output is computationally unpredictable: knowing every value
 * it has produced so far tells you nothing useful about the next one.
 *
 * Every bounded draw below uses rejection sampling rather than modulo. Naive
 * modulo is measurably biased -- for a bound of 100 some outcomes come up nearly
 * twice as often as others -- and the bias is invisible unless you go looking.
 */
object Rng {

    private val secure = SecureRandom()

    /** Uniform in [0, bound). Delegates to Random.nextInt, which already rejects. */
    fun int(bound: Int): Int {
        require(bound > 0) { "bound must be positive" }
        return secure.nextInt(bound)
    }

    /** Uniform in [0, bound), unbiased across the full Long range. */
    fun longBelow(bound: Long): Long {
        require(bound > 0L) { "bound must be positive" }
        while (true) {
            val bits = secure.nextLong() ushr 1          // non-negative
            val value = bits % bound
            // Overflows to negative only when the draw landed in the
            // incomplete final block, which is exactly what we reject.
            if (bits - value + (bound - 1L) >= 0L) return value
        }
    }

    /** Uniform integer, inclusive of both endpoints, safe across the whole Int range. */
    fun intInRange(lo: Int, hi: Int): Int {
        val low = minOf(lo, hi)
        val high = maxOf(lo, hi)
        val span = high.toLong() - low.toLong() + 1L
        return (low.toLong() + longBelow(span)).toInt()
    }

    /** Uniform Long, inclusive of both endpoints. */
    fun longInRange(lo: Long, hi: Long): Long {
        val low = minOf(lo, hi)
        val high = maxOf(lo, hi)
        return low + longBelow(high - low + 1L)
    }

    fun bool(): Boolean = secure.nextBoolean()

    /** Uniform in [0.0, 1.0). */
    fun double(): Double = secure.nextDouble()

    /** Fisher-Yates, driven by the same entropy source. */
    fun <T> shuffled(items: List<T>): List<T> {
        val out = items.toMutableList()
        for (i in out.indices.reversed()) {
            val j = secure.nextInt(i + 1)
            val tmp = out[i]
            out[i] = out[j]
            out[j] = tmp
        }
        return out
    }

    fun <T> pick(items: List<T>): T = items[secure.nextInt(items.size)]

    fun pickChar(source: String): Char = source[secure.nextInt(source.length)]
}
