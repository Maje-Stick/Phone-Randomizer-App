# Compose and Kotlin metadata that R8 must not strip.
-dontwarn org.jetbrains.annotations.**
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}
-keep class androidx.compose.runtime.** { *; }

# Crash reports are useless without real line numbers.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
