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