# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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

# ez-vcard
-dontwarn ezvcard.io.json.**            # JSON serializer (for jCards) not used
-dontwarn freemarker.**                 # freemarker templating library (for creating hCards) not used
-dontwarn org.jsoup.**                  # jsoup library (for hCard parsing) not used
-dontwarn sun.misc.Perf
-keep,includedescriptorclasses class ezvcard.property.** { *; }  # keep all VCard properties (created at runtime)

#Google and Firebase
-keep class com.google.**
-dontwarn com.google.**
#SearchView
-keep class android.support.v7.widget.** { *; }

#Eventbus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#Matisse
-dontwarn com.squareup.picasso.**
-dontwarn com.zhihu.matisse.**



#Sinch
-keepclasseswithmembernames class * {native <methods>;}
-dontwarn org.apache.http.annotation.**
-keep class com.sinch.** { *; }
-keep interface com.sinch.** { *; }
-keep class org.webrtc.** { *; }
-dontwarn android.media.**
