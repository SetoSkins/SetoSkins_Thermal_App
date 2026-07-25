# 保留行号信息，便于调试线上崩溃
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Miuix 组件库
-keep class top.yukonga.miuix.kmp.** { *; }

# 保留四大组件
-keep class com.setoskins.thermal.MainActivity { *; }
-keep class com.setoskins.thermal.service.** { *; }

# 保留 ModuleDetector 中的公开方法（反射/动态调用风险）
-keep class com.setoskins.thermal.data.** { *; }

# 保留 DataStore 序列化
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# 移除 Kotlin 元数据（减小体积，不影响运行时）
-keepattributes *Annotation*
-dontnote kotlin.Metadata

# ══════════════════════════════════════════════
# 体积优化
# ══════════════════════════════════════════════

# 移除所有日志输出
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# 移除 Kotlin 反射（如未使用）
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullParameter(java.lang.Object, java.lang.String);
}

# 移除 Kotlinx 序列化未使用代码
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.setoskins.thermal.**$$serializer { *; }
-keepclassmembers class com.setoskins.thermal.** {
    *** Companion;
}
-keepclasseswithmembers class com.setoskins.thermal.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontnote kotlinx.serialization.**

# 激进优化
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-repackageclasses com.setoskins.thermal

# 移除未使用的 Kotlin stdlib
-assumenosideeffects class kotlin.collections.CollectionsKt {
    static *** emptyList();
    static *** emptySet();
    static *** emptyMap();
}