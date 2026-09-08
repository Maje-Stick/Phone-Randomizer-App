package com.majestick.randomizer

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * An in-memory ring buffer of everything the app does, plus two catchers for the
 * cases where the app can no longer tell you anything itself:
 *
 *  - **Crash.** An uncaught-exception handler writes the buffer and the stack
 *    trace to disk before the process dies, so it is waiting in the Debug screen
 *    the next time the app opens.
 *  - **Freeze.** A watchdog thread pings the main thread every half second. If
 *    the ping goes unanswered for three seconds, it dumps the main thread's
 *    stack -- which is the single most useful thing to have, because it names
 *    the exact line the UI is stuck on.
 *
 * Nothing here touches gesture or focus code paths. It only observes.
 */
object DebugLog {

    private const val CAPACITY = 2500
    private const val PREFS = "debug_log"
    private const val KEY_VERBOSE = "verbose"
    private const val REPORT_FILE = "last_report.txt"
    private const val STALL_MS = 3000L

    private val stamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val lines = ArrayDeque<String>()
    private val lock = Any()

    private var appContext: Context? = null
    private var prefs: SharedPreferences? = null
    private var installed = false
    private val startedAt = SystemClock.uptimeMillis()

    /** Report left behind by the last crash or freeze. Loaded once at startup. */
    var lastReport: String? = null
        private set

    private var verboseField = true

    /**
     * Gesture and focus tracing. Loud on purpose -- a held button logs roughly
     * fourteen lines a second -- so it can be turned off once a bug is pinned.
     */
    var verbose: Boolean
        get() = verboseField
        set(value) {
            verboseField = value
            prefs?.edit()?.putBoolean(KEY_VERBOSE, value)?.apply()
            log("debug", "verbose tracing ${if (value) "ON" else "OFF"}")
        }

    fun install(context: Context) {
        if (installed) return
        installed = true
        val app = context.applicationContext
        appContext = app
        prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        verboseField = prefs?.getBoolean(KEY_VERBOSE, true) ?: true
        loadReport()
        log("app", "process start")
        log("app", header().trim().replace("\n", " | "))
        if (lastReport != null) log("app", "a previous crash/freeze report is waiting")
        installCrashHandler()
        startWatchdog()
    }

    // ---------------------------------------------------------------- writing

    fun log(tag: String, message: String) {
        val line = synchronized(lock) {
            val text = "${stamp.format(Date())}  ${tag.padEnd(11)}  $message"
            if (lines.size >= CAPACITY) lines.removeFirst()
            lines.addLast(text)
            text
        }
        android.util.Log.d("Randomizer", line)
    }

    /** High-frequency tracing. Silent unless [verbose] is on. */
    fun trace(tag: String, message: String) {
        if (verboseField) log(tag, message)
    }

    // ---------------------------------------------------------------- reading

    fun linesSnapshot(): List<String> = synchronized(lock) { lines.toList() }

    fun text(): String = synchronized(lock) { lines.joinToString("\n") }

    fun size(): Int = synchronized(lock) { lines.size }

    fun fullReport(): String = buildString {
        appendLine("=== Randomizer log ===")
        append(header())
        appendLine()
        append(text())
    }

    fun header(): String {
        val c = appContext
        val version = c?.let {
            runCatching {
                it.packageManager.getPackageInfo(it.packageName, 0).versionName
            }.getOrNull()
        } ?: "unknown"
        val up = (SystemClock.uptimeMillis() - startedAt) / 1000
        return buildString {
            appendLine("Randomizer $version")
            appendLine("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            appendLine("${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Running for ${up}s, ${size()} lines buffered")
        }
    }

    fun clear() {
        synchronized(lock) { lines.clear() }
        lastReport = null
        appContext?.let { runCatching { File(it.filesDir, REPORT_FILE).delete() } }
        log("debug", "log cleared")
    }

    // ---------------------------------------------------------------- reports

    private fun writeReport(kind: String, detail: String) {
        val text = buildString {
            appendLine("=== $kind ===")
            append(header())
            appendLine()
            appendLine(detail)
            appendLine()
            appendLine("--- log leading up to it ---")
            append(text())
        }
        lastReport = text
        val c = appContext ?: return
        runCatching { File(c.filesDir, REPORT_FILE).writeText(text) }
    }

    private fun loadReport() {
        val c = appContext ?: return
        runCatching {
            val file = File(c.filesDir, REPORT_FILE)
            if (file.exists()) lastReport = file.readText()
        }
    }

    // ---------------------------------------------------------------- catchers

    private fun installCrashHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                log("CRASH", "${error.javaClass.name}: ${error.message}")
                writeReport(
                    "CRASH on thread '${thread.name}'",
                    stackOf(error)
                )
            }
            if (previous != null) {
                previous.uncaughtException(thread, error)
            } else {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }

    private fun startWatchdog() {
        val main = Handler(Looper.getMainLooper())
        val answered = AtomicBoolean(true)
        val worker = Thread {
            var lastAnswer = SystemClock.uptimeMillis()
            var reported = false
            while (!Thread.currentThread().isInterrupted) {
                if (answered.compareAndSet(true, false)) {
                    val stalled = SystemClock.uptimeMillis() - lastAnswer
                    if (reported) {
                        log("FREEZE", "main thread recovered after ${stalled}ms")
                        reported = false
                    }
                    lastAnswer = SystemClock.uptimeMillis()
                    main.post { answered.set(true) }
                } else {
                    val stalled = SystemClock.uptimeMillis() - lastAnswer
                    if (stalled > STALL_MS && !reported) {
                        reported = true
                        val stack = mainStack()
                        log("FREEZE", "main thread unresponsive for ${stalled}ms")
                        writeReport(
                            "FREEZE -- main thread blocked ${stalled}ms",
                            "Main thread stack at the moment it hung:\n$stack"
                        )
                    }
                }
                runCatching { Thread.sleep(500) }.onFailure { return@Thread }
            }
        }
        worker.name = "randomizer-watchdog"
        worker.isDaemon = true
        worker.start()
    }

    private fun mainStack(): String =
        Looper.getMainLooper().thread.stackTrace
            .take(35)
            .joinToString("\n") { "    at $it" }

    private fun stackOf(error: Throwable): String {
        val writer = StringWriter()
        error.printStackTrace(PrintWriter(writer))
        return writer.toString()
    }
}
