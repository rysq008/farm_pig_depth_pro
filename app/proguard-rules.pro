# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-dontoptimize
#-dontpreverify
#-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

#luolu
#-optimizationpasses 5
#-keep class com.xiangchuangtec.luolu.animalcounter.** { *; }
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-verbose
#
## ============忽略警告，否则打包可能会不成功=============
#-ignorewarnings
#AnimalAI proguard
-keepattributes SourceFile,LineNumberTable
-repackageclasses a

-keepclasseswithmembernames class * {
    native <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.xmlpull.v1.**
-dontwarn org.kobjects.**
-dontwarn org.ksoap2.**
-dontwarn org.kxml2.**

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontoptimize
-dontpreverify