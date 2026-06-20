# youtubedl-android relies on reflection / JNI into the bundled Python payload.
# Keep its classes to be safe even though release minify is currently off.
-keep class com.yausername.** { *; }
-dontwarn com.yausername.**
