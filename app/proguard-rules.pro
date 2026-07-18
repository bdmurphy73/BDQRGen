# UmbraQRGen ProGuard Rules

# Keep ZXing QR code library intact
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Keep app entry points and public API
-keep class com.umbratools.umbraqrgen.MainActivity { *; }
-keep class com.umbratools.umbraqrgen.AppVersion { *; }

# Keep Compose runtime classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep ViewModel classes (needed for viewModel() factory)
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep FileProvider (required for sharing files via URI)
-keep class androidx.core.content.FileProvider { *; }

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Keep line numbers in stack traces for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Suppress warnings for unused libraries
-dontwarn kotlin.Unit
-dontwarn kotlin.reflect.**
