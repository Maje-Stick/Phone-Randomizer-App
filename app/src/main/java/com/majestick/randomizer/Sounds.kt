package com.majestick.randomizer

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.SoundEffectConstants
import android.view.View

/**
 * Two sounds, deliberately.
 *
 * Ordinary taps use the platform's own click effect, which means they follow
 * whatever the user already chose in system sound settings and cost nothing to
 * ship. Only the draw button gets a custom sound, because that is the one moment
 * the app should feel like it did something.
 */
object Sounds {

    private const val PREFS = "randomizer_settings"
    private const val KEY = "sounds_enabled"

    private var pool: SoundPool? = null
    private var chimeId = 0
    private var ready = false

    private var enabledField = true
    private var prefs: android.content.SharedPreferences? = null

    var enabled: Boolean
        get() = enabledField
        set(value) {
            enabledField = value
            prefs?.edit()?.putBoolean(KEY, value)?.apply()
            DebugLog.log("sound", "sounds ${if (value) "on" else "off"}")
        }

    fun init(context: Context) {
        if (pool != null) return
        val app = context.applicationContext
        prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        enabledField = prefs?.getBoolean(KEY, true) ?: true

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val created = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attributes)
            .build()
        created.setOnLoadCompleteListener { _, _, status ->
            ready = status == 0
            DebugLog.log("sound", if (ready) "chime loaded" else "chime failed to load")
        }
        chimeId = created.load(app, R.raw.draw_chime, 1)
        pool = created
    }

    /** A tap. Silent if the user turned sounds off, or if the system has. */
    fun click(view: View) {
        if (enabledField) view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    /** The moment a result appears. */
    fun draw() {
        if (!enabledField || !ready) return
        pool?.play(chimeId, 0.55f, 0.55f, 1, 0, 1f)
    }
}
