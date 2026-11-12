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

# Fast tree-shaking for unused material-icons-extended classes without obfuscation or optimization
-dontobfuscate
-dontoptimize
-dontpreverify
-dontwarn **
-ignorewarnings

-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature,SourceFile,LineNumberTable

-keep class com.example.** { *; }
-keep interface com.example.** { *; }
-keep enum com.example.** { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class kotlinx.serialization.** { *; }
-keep class androidx.room.** { *; }

