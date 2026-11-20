-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Compose 与 Miuix 均为纯 Kotlin/Compose 实现，不整体 keep，
# 交给 R8 按实际引用裁剪（可去掉 material-icons-extended 中上万条未使用图标类）。
-dontwarn androidx.compose.**
-dontwarn top.yukonga.miuix.kmp.**

-dontwarn kotlinx.coroutines.**

-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

-keepattributes *Annotation*
-dontnote kotlin.Metadata

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullParameter(java.lang.Object, java.lang.String);
}

# 覆盖 proguard-android-optimize.txt 的默认设置，重新允许 class/merging，
# 并允许修改访问权限、合并类，进一步压缩 dex。
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*
-optimizationpasses 5
-allowaccessmodification
-repackageclasses com.setoskins.thermal

-assumenosideeffects class kotlin.collections.CollectionsKt {
    static *** emptyList();
    static *** emptySet();
    static *** emptyMap();
}
