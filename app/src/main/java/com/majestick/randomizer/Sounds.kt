package com.majestick.randomizer

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.View

/**
 * Two sounds per theme: a short tap and a longer draw.
 *
 * The set follows whichever theme is selected, so the Sea theme bubbles and the
 * Night City theme blips. Plain colour themes use the neutral set. Everything is
 * synthesised and shipped as small WAVs -- the whole library is under 150KB.
 *
 * Taps are deliberately tiny and quiet. They fire constantly, and anything with
 * a tail becomes unbearable within a minute.
 */
object Sounds {

    private const val PREFS = "randomizer_settings"
    private const val KEY = "sounds_enabled"

    /** Theme id -> sound set suffix. Anything unlisted falls back to default. */
    private val SETS = mapOf(
        "sea" to "sea",
        "space" to "space",
        "nature" to "nature",
        "autumn" to "autumn",
        "citynight" to "citynight",
        "aero" to "aero",
        "digital" to "digital"
    )

    private var pool: SoundPool? = null
    private val draws = HashMap<String, Int>()
    private val taps = HashMap<String, Int>()
    private val loaded = HashSet<Int>()

    private var prefs: SharedPreferences? = null
    private var enabledField = true
    private var setName = "default"

    var enabled: Boolean
        get() = enabledField
        set(value) {
            enabledField = value
            prefs?.edit()?.putBoolean(KEY, value)?.apply()
            DebugLog.log("sound", "sounds ${if (value) "on" else "off"}")
        }

    /** Called whenever the theme changes so the set follows it. */
    fun useTheme(themeId: String?) {
        val next = SETS[themeId] ?: "default"
        if (next != setName) {
            setName = next
            DebugLog.log("sound", "sound set -> $next")
        }
    }

    fun init(context: Context) {
        if (pool != null) return
        val app = context.applicationContext
        prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        enabledField = prefs?.getBoolean(KEY, true) ?: true

        val created = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        created.setOnLoadCompleteListener { _, id, status ->
            if (status == 0) loaded.add(id)
        }

        val names = listOf("default") + SETS.values
        for (name in names) {
            resource(app, "draw_$name")?.let { draws[name] = created.load(app, it, 1) }
            resource(app, "tap_$name")?.let { taps[name] = created.load(app, it, 1) }
        }
        pool = created
        DebugLog.log("sound", "loaded ${draws.size} draw and ${taps.size} tap sounds")
    }

    @Suppress("DiscouragedApi")
    private fun resource(context: Context, name: String): Int? {
        val id = context.resources.getIdentifier(name, "raw", context.packageName)
        return if (id != 0) id else null
    }

    /** A tap. Falls back to the platform click if the set has not loaded yet. */
    fun click(view: View) {
        if (!enabledField) return
        val id = taps[setName] ?: taps["default"]
        if (id != null && id in loaded) {
            pool?.play(id, 0.28f, 0.28f, 0, 0, 1f)
        } else {
            view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
        }
    }

    /** The moment a result appears. */
    fun draw() {
        if (!enabledField) return
        val id = draws[setName] ?: draws["default"] ?: return
        if (id in loaded) pool?.play(id, 0.55f, 0.55f, 1, 0, 1f)
    }
}
