# Release builds run R8 with minification and resource shrinking. Everything
# below is something whose *name* is data rather than an implementation detail,
# so renaming it breaks the app in ways that do not crash — it just quietly
# does the wrong thing, which is harder to notice and worse.

# ---------------------------------------------------------------------------
# Enums persisted by name
#
# Room stores these through TypeConverters that call .name and valueOf(), and
# UserPrefs does the same into DataStore. If R8 renames a constant, a release
# build writes obfuscated names into the database — and every converter ends in
# runCatching { ... }.getOrDefault(...), so the failure is invisible: every
# exercise silently becomes Equipment.OTHER and every set NORMAL.
# ---------------------------------------------------------------------------
-keepclassmembers enum com.sparkgym.domain.model.** { *; }
-keepclassmembers enum com.sparkgym.domain.engine.** { *; }
-keepclassmembers enum com.sparkgym.core.util.** { *; }

# ---------------------------------------------------------------------------
# The WebView bridge
#
# The 3D heat map calls AndroidBridge.onMuscleTapped from JavaScript, by name.
# R8 cannot see that call, so without this it renames or removes the method —
# and the page guards with `if (window.AndroidBridge && ...)`, so tapping a
# muscle would simply stop doing anything, with nothing in the log.
# ---------------------------------------------------------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ---------------------------------------------------------------------------
# kotlinx.serialization
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.sparkgym.data.remote.** {
    *** Companion;
}
-keepclasseswithmembers class com.sparkgym.data.remote.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sparkgym.data.remote.**$$serializer { *; }

# ---------------------------------------------------------------------------
# Retrofit / OkHttp
# ---------------------------------------------------------------------------
-keepattributes Signature, RuntimeVisibleAnnotations, AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# Retrofit reads the annotations off its interface methods reflectively.
-keep,allowobfuscation interface com.sparkgym.data.remote.*Api
-dontwarn okhttp3.**
-dontwarn okio.**

# ---------------------------------------------------------------------------
# Room
#
# The generated *_Impl classes are looked up by name from the @Database class.
# ---------------------------------------------------------------------------
-keep class com.sparkgym.data.local.**_Impl { *; }

# Health Connect is optional at runtime — the app checks availability before
# touching it — so a missing provider must not fail the build.
-dontwarn androidx.health.connect.**
