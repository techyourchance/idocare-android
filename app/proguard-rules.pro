# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Program Files (x86)\Android\android-studio\sdk/tools/proguard/proguard-android.txt
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


## --------------------------------------------------------------------------------
#
# Ease future debugging
#
## --------------------------------------------------------------------------------

-keepattributes SourceFile,LineNumberTable

## --------------------------------------------------------------------------------
#
# Clear logging statements
#
## --------------------------------------------------------------------------------

# Default Android logger
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Our custom non-static logger
-assumenosideeffects class il.co.idocare.utils.Logger {
    public *** d(...);
    public *** v(...);
    public *** i(...);
    public *** w(...);
    public *** e(...);
}

## --------------------------------------------------------------------------------
#
# The below ignore roundedimageview errors
#
# TODO: remove this lib in favor of Picasso and remove this section
#
## --------------------------------------------------------------------------------

-dontwarn com.makeramen.roundedimageview.**

## --------------------------------------------------------------------------------
#
# org.apache.commons.validator package references many other org.apache.commons
# packages, which are not included in the app. This makes ProGuard go crazy.
# Disable warnings for org.apache.commons altogether.
#
## --------------------------------------------------------------------------------

-dontwarn org.apache.commons.**

## --------------------------------------------------------------------------------
#
# Rules for joda-time datetime library
#
## --------------------------------------------------------------------------------

-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString

## --------------------------------------------------------------------------------
#
# The below were taken from Retrofit 2 official page
#
## --------------------------------------------------------------------------------

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on RoboVM on iOS. Will not be used at runtime.
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

## --------------------------------------------------------------------------------
#
# Additional config for Retrofit
#
## --------------------------------------------------------------------------------

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

## --------------------------------------------------------------------------------
#
# The below were taken from GreenRobot EventBus official page
#
## --------------------------------------------------------------------------------

-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { * ; }

## --------------------------------------------------------------------------------
#
# The below ignore okio warnings
#
## --------------------------------------------------------------------------------

-dontwarn okio.**
