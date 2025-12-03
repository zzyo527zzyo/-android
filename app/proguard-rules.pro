# 保留 Application
-keep class android.app.Application { *; }

# 保留 MainActivity（入口）
-keep class com.example.secupay_jni.Activity.shield_testActivity { *; }

# 保留 Activity 和 Fragment
-keep class * extends android.app.Activity
-keep class * extends androidx.fragment.app.Fragment

# 保留 Kotlin
-keep class kotlin.** { *; }
-keep class androidx.** { *; }
-keep class kotlinx.** { *; }

# 保留注解和 Lambda
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.jetbrains.annotations.Nullable <fields>;
    @org.jetbrains.annotations.NotNull <fields>;
    @org.jetbrains.annotations.Nullable <methods>;
    @org.jetbrains.annotations.NotNull <methods>;
}