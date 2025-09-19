# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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

# FreeReflection
-keep class me.weishu.reflection.** {*;}

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static *** throwUninitializedProperty(...);
    public static *** throwUninitializedPropertyAccessException(...);
}

-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    *** inflate(android.view.LayoutInflater);
}

-keep class * extends android.app.Activity
-keep class * implements androidx.viewbinding.ViewBinding {
    <init>();
    *** inflate(android.view.LayoutInflater);
}


# DexKit
-keep class org.luckypray.dexkit.DexKitBridge {
    native <methods>;
}


# for logback
# Issue #229
-keep class ch.qos.logback.classic.pattern.* { <init>(); }
# 如果确实使用 logback/SLF4J，可保留成员
-keep class ch.qos.logback.** { *; }
-keep class org.slf4j.impl.** { *; }
# 保留注解
-keepattributes *Annotation*

# 忽略 javax.mail 相关缺失
-dontwarn javax.mail.**
-dontwarn javax.activation.**

# 忽略 DexKit/YukiHookAPI 引用的 Java 8 反射类
-dontwarn java.lang.reflect.AnnotatedType
-dontwarn java.lang.reflect.Executable
-dontwarn java.lang.reflect.Parameter
-dontwarn java.lang.reflect.Type