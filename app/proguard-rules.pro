-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

-keep class top.yukonga.miuix.kmp.** { *; }

-keep class com.setoskins.thermal.MainActivity { *; }
-keep class com.setoskins.thermal.service.** { *; }

-keep class com.setoskins.thermal.data.** { *; }

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

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-repackageclasses com.setoskins.thermal

-assumenosideeffects class kotlin.collections.CollectionsKt {
    static *** emptyList();
    static *** emptySet();
    static *** emptyMap();
}
