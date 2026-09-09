# Compose and Kotlin metadata that R8 must not strip.
-dontwarn org.jetbrains.annotations.**
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}
-keep class androidx.compose.runtime.** { *; }

# Crash reports are useless without real line numbers.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Resolved by name at runtime (Sounds, ThemeBackdrop), so R8 cannot see the
# reference. res/raw/keep.xml covers the resources; this covers the lookup.
-keep class com.majestick.randomizer.R$raw { *; }
-keep class com.majestick.randomizer.R$drawable { *; }
