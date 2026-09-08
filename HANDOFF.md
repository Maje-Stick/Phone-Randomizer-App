# Sweet Simple Randomizer — handoff

Paste this at the start of a new chat to pick the project up cold.

For the current version, read `app/build.gradle.kts` after cloning — a number
written here would be stale the moment this file ships inside a build.

---

## Who and what

Majestick, working entirely from an Android phone. No PC anywhere in this loop.
The app is a randomizer with ten tools, Kotlin + Jetpack Compose, single module.

- Launcher label: **SS Randomizer**
- Store / official name: **Sweet Simple Randomizer**
- Package id: `com.majestick.randomizer` — **never change this**, it is permanent once published
- Repo: `https://github.com/Maje-Stick/Phone-Randomizer-App` (public)

The goal now is a Play Store release, which will be called **version 1.00**.
Everything before that ships as 2.x, which is confusing but deliberate — the 2.x
line is the pre-release engineering history.

## How work actually gets done

There is no local build. The loop is:

1. Claude clones the public repo, edits, and returns a zip.
2. Majestick extracts it over his working copy and pushes.
3. GitHub Actions builds a signed debug APK.
4. He downloads the artifact and installs it.

**Always clone the repo before doing anything.** Never work from memory of the
code — it has been wrong before.

His commands, in order:

```
cd ~/randomizer
unzip -o ~/storage/downloads/randomizer-vX.Y.zip -d ~
git add -A && git commit -m "..." && git push
```

**`unzip -o` never deletes.** If a change removes a file, say so explicitly and
give the `git rm` line. A stray `draw_chime.wav` survived three versions this way.

Bump `versionCode` and `versionName` in `app/build.gradle.kts` on every zip, so
he can confirm from app info which build he is actually running. This has
mattered — a whole debugging round was once spent on an unconfirmed install.

### When the build goes red

He says "red circle". Ask for the log, do not guess:

```
cd ~/storage/downloads && rm -rf ci-logs_* && for z in logs_*.zip; do unzip -oq "$z" -d "ci-${z%.zip}"; done
grep -rhE "e: file|error:|Unresolved|FAILURE" ci-logs_* | head -30
```

Note `^e:` will not match — GitHub prefixes every line with a timestamp. Check
timestamps, since old archives linger.

## Signing — read this before touching CI

The release key is **private** and lives only in GitHub Actions secrets:
`SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`,
`SIGNING_KEY_PASSWORD`. `app/build.gradle.kts` reads them from the environment
and falls back to the debug key when absent.

An earlier version committed the keystore *and* its password to the public repo.
That is fixed, but the dead key remains in git history — harmless, and not worth
rewriting history over. Never reuse `randomizer` as a password.

The keystore file is at `~/ssr-release.p12` on his phone. If it is ever lost
after publishing, the app can never be updated. He should enrol in Play App
Signing at first upload, which makes that recoverable.

## Code map

| File | What lives there |
|---|---|
| `MainActivity.kt` | Screen routing, home grid, drag-to-reorder, settings |
| `Ui.kt` | `ToolScaffold`, `tapOrHoldStep`, `EditableNumber`, shared controls |
| `ResultViews.kt` | Result rendering per tool, `ResultFrame` and its copy button |
| `EntryEditor.kt` | Weighted entry list, weight bars, text view, `mergeFromText` |
| `ToolScreens.kt` | Per-tool option screens |
| `Randomizers.kt` | The actual randomization for each tool |
| `Rng.kt` | `SecureRandom`, unbiased ranges. Do not "simplify" this |
| `Themes.kt` | Palettes, `RandomizerTheme`, `ThemeBackdrop` |
| `Sounds.kt` | Per-theme tap and draw sounds via SoundPool |
| `DebugLog.kt` / `DebugScreen.kt` | Ring buffer, crash handler, freeze watchdog, viewer |
| `HelpScreen.kt` | The user manual |
| `Presets.kt`, `Drafts.kt`, `ToolOrder.kt` | Persistence |

## The one open item: artwork

Seven artistic themes exist (`nature`, `sea`, `space`, `citynight`, `autumn`,
`aero`, `digital`) and currently render as gradients. They are built to accept
real artwork with **no code change**.

Drop a PNG named `art_<theme>_<screen>.png` into `app/src/main/res/drawable` and
it appears. Screens are `home`, `tool`, `settings`, `help`, `debug`; a missing
screen falls back to that theme's `home` image. A scrim is applied automatically
so text stays readable — if it reads wrong, tune the alphas in `ThemeBackdrop`.

Claude **cannot generate images**. Majestick has Hugging Face Pro and generates
them himself with Flux. Prompts should ask for dark, muted, low-contrast,
portrait. `res/raw/keep.xml` stops R8 stripping them, since they are resolved by
name at runtime.

## Hard-won lessons

**Custom pointer handling is where this project bleeds.** Four separate failures.
The working pattern is `tapOrHoldStep` in `Ui.kt`: it measures the finger's own
travel against `viewConfiguration.touchSlop`, consumes nothing, and lets the
parent scroll arbitrate. Do not replace it with `detectTapGestures` — on this
device `tryAwaitRelease()` reports *every* press as cancelled, real taps included,
because the targets are 24dp wide and a fingertip roll leaves their bounds.
Drag-to-reorder works because `detectDragGesturesAfterLongPress` claims nothing
until the long press succeeds, plus `userScrollEnabled = draggingKey == null`.

**Most build failures came from how files were edited, not what was written.**
In order: a missing import; an annotation swallowed because the insertion point
matched text that had `@OptIn` above it; a regex that ate a bracket while
rewriting 19 call sites. **Do not do regex rewrites across many call sites.**
Read each site and edit it explicitly, or restructure so one place changes.
Brace-balance checks do not catch a `)` swapped for a `}` — the totals stay even.

**Run these audits before zipping.** Two classes of error were invisible for
most of the project:

- Modifier extensions are written `.width(...)`, so any check that skips
  identifiers preceded by a dot cannot see them. Check them by name against a
  known list.
- Every function using `TopAppBar` needs `@OptIn(ExperimentalMaterial3Api::class)`
  directly above it. Collect the annotations above each function and verify.

**Verify algorithms by porting them to Python and running them.** This caught
nothing wrong in `mergeFromText` but proved nine cases, and the reorder swap maths
was validated for oscillation the same way. Cheap, and it is the only execution
available.

**Say plainly when something is a guess.** Nothing here is compile-checked. The
container has no Android SDK.

**Ask before downloading or uploading anything over 20MB**, including batches.

## Debug log workflow

Settings → Diagnostics → Debug log. Tracing defaults to **off** now that the
gesture bugs are closed; it is a toggle away.

The workflow that matters: **Mark**, reproduce the bug, **Copy since mark**. That
is dozens of lines instead of thousands. Do not ask for the whole log — it wastes
his usage limits and buries the signal.

Crashes and freezes are captured automatically. A watchdog dumps the main thread's
stack after three seconds unresponsive, which names the exact line the UI hung on.

## Still open

- Artwork for the seven artistic themes
- General polish pass — deliberately vague, wants specific complaints not a sweep
- R8 is enabled for release but **never exercised**, since CI only builds debug.
  Check the app still runs the first time a release bundle is built
- Decide public vs private repo. Currently public. Going private would mean
  Claude cannot clone, and he would upload a zip each session instead
