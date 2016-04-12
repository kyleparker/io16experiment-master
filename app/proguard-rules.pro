# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\development\android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontskipnonpubliclibraryclasses
-verbose

-dontpreverify

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
# change from 5 to 2. With 5 passes, the CursorLoader in the android.support.v4 will not compile.
# See http://stackoverflow.com/questions/6605971/android-sdk-tools-revision-12-has-problem-with-proguard-error-conversion-to
-optimizationpasses 2
-allowaccessmodification

-keepattributes *Annotation*
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.view.** { *; }
-keep public class * extends android.app.Fragment

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
#
# Section 2
# Google APIs Client Library for Java proguard settings.
#

# Needed by google-api-client to keep generic types and @Key annotations accessed via reflection
-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault

# See https://groups.google.com/forum/#!topic/guava-discuss/YCZzeCiIVoI
-dontwarn com.google.common.collect.MinMaxPriorityQueue

# For Google Play Services
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *

-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# For Android Design Support Library
-keep class android.support.design.widget.** { *; }
-keep class android.support.v7.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }
-keep interface android.support.v7.design.widget.** { *; }
-dontwarn android.support.design.**
-dontwarn android.support.v7.design.**

# Standard Android proguard settings
-keep public class * extends android.support.v4.view.ViewPager { *; }
-keep public class com.google.** { *; }

# Firebase
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
-dontwarn com.firebase.ui.auth.**

# http://stackoverflow.com/questions/29691905/android-app-crashes-when-proguard-is-enabled
-keep public class com.fourteenelevendev.android.apps.model.** { *; }

# Make our stack traces useful
# Line numbers will be correct, file names will be replaced by "TinCanIOExperiment" since the
# class name is enough to get the file name.
-renamesourcefileattribute TinCanIOExperiment