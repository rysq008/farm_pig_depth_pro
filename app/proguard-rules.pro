# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\sdk/tools/proguard/proguard-android.txt
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

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
#-dontpreverify
-dontwarn
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# android proguard -------------------------------------
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.app.Fragment
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.support.v4.view.ViewPager
-keep public class * extends android.os.HandlerThread

-dontwarn com.android.volley.**
-keep class com.android.volley.**{*;}
-dontwarn com.tencent.**
-keep class com.tencent.**{*;}
-dontwarn com.google.zxing.**
-keep class com.google.zxing.**{*;}

-keep class com.innovation.pig.insurance.model.**{*;}
-keep class com.innovation.pig.insurance.view.**{*;}
-keep public class com.innovation.pig.insurance.AppConfig {*;}
-keep class com.serenegiant.widget.**{*;}
-keep class com.serenegiant.**{*;}
-keep class com.xiangchuang.risks.model.bean.**{*;}
-keep class com.xiangchuang.risks.model.custom.**{*;}
#模型识别
-keep class innovation.biz.**{*;}
-keep class innovation.database.**{*;}
-keep public class innovation.crash.CrashHandler{*;}
-keep class innovation.database.**{*;}
-keep class innovation.entry.**{*;}
-keep class innovation.env.**{*;}
-keep class innovation.view.**{*;}
-keep class org.opencv.**{*;}
-keep public class org.tensorflow.demo.env.ImageUtils {*;}
-keep public class org.tensorflow.demo.tracking.ObjectTracker {*;}
-keep public class org.tensorflow.demo.AutoFitTextureView {*;}
-keep public class org.tensorflow.demo.ImageUtils {*;}
-keep public class org.tensorflow.demo.ObjectTracker {*;}
-keep public class org.tensorflow.demo.OverlayView {*;}
-keep public class org.tensorflow.demo.tracking.**{*;}
-keep public class com.innovation.pig.insurance.netutils.PreferencesUtils {*;}

-keep public class com.farm.innovation.base.FarmAppConfig {*;}
-keep class com.farm.innovation.bean.**{*;}
-keep class com.farm.innovation.biz.Insured.**{*;}
-keep class com.farm.innovation.biz.classifier.**{*;}
-keep class com.farm.innovation.biz.iterm.**{*;}
-keep class com.farm.innovation.crash.**{*;}
-keep class com.farm.innovation.login.model.**{*;}
-keep class com.farm.innovation.model.user.**{*;}
-keep class com.farm.innovation.view.**{*;}

-keep class com.mainaer.wjoklib.okhttp.**{*;}
-keep public class com.farm.innovation.utils.FarmerPreferencesUtils {*;}

-keep class thirdparty.bottombar.library.**{*;}

-keep class com..newapk.activity.BaseWebNewViewActivity$*{

    *;

 }
-keep class com.tribediandiangou.activity.BaseWebViewActivity$*{

    *;

 }

#删除日志
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String,int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# okhttp 不混淆
-keep class okhttp3.**{*;}

#避免混淆泛型 如果混淆报错建议关掉
-keepattributes Signature

-keepattributes Exceptions,InnerClasses
-keep public class com.alipay.android.app.** {
    public <fields>;
    public <methods>;
}

#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepattributes Exceptions,InnerClasses
-keep public class com.alipay.android.app.** {
    public <fields>;
    public <methods>;
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

-keepclasseswithmembers,allowshrinking class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keepclasseswithmembers,allowshrinking class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keep class data.db.dao.*$Properties {
    public static <fields>;
}
-keepclassmembers class data.db.dao.** {
    public static final <fields>;
  }

-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * extends android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-ignorewarning
-keep public class * extends android.widget.TextView


-keep class com.alipay.android.app.** {
    public <fields>;
    public <methods>;
}

-keep class com.alipay.sdk.** {
    public <fields>;
    public <methods>;
}

-keep class com.alipay.mobilesecuritysdk.** {
    public <fields>;
    public <methods>;
}

-keep class HttpUtils.** {
    public <fields>;
    public <methods>;
}

#umeng
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-keep public class javax.**
-keep public class android.webkit.**
-dontwarn android.support.v4.**
-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {*;}


-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}

-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}

-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}

-dontwarn com.tencent.**

-keep public class com.umeng.soexample.R$*{
     public static final int *;
 }
-keep public class com.umeng.soexample.R$*{
     public static final int *;
 }
-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}

-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class  com.alipay.share.sdk.** {
    *;
 }
 -keepnames class * implements android.os.Parcelable {
     public static final ** CREATOR;
}


-keep class com.switfpass.pay.**{*;}

-keep class com.linkedin.** { *; }
-keepattributes Signature

#greendao proguard -------------------------------------------------------
-keep class org.greenrobot.greendao.**{*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties

# If you do not use SQLCipher:
 -dontwarn org.greenrobot.greendao.database.**
 -keep class net.sqlcipher.** {
     *;
 }

 -keep class net.sqlcipher.** { *; }
 -keep class net.sqlcipher.database.* { *; }
 #-keep class com.laikan.reader.database.** { *; }

# If you do not use RxJava:
-dontwarn rx.**

# eventbus proguard ---------------------------------------
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(Java.lang.Throwable);
}

# jjwt proguard -------------------------------------------
-keepnames class com.fasterxml.jackson.databind.** { *; }
-dontwarn com.fasterxml.jackson.databind.*
-keepattributes InnerClasses

-keep class org.bouncycastle.** { *; }
-keepnames class org.bouncycastle.* { *; }
-dontwarn org.bouncycastle.*

-keep class io.jsonwebtoken.** { *; }
-keepnames class io.jsonwebtoken.* { *; }
-keepnames interface io.jsonwebtoken.* { *; }

-dontwarn javax.xml.bind.DatatypeConverter
-dontwarn io.jsonwebtoken.impl.Base64Codec

-keepnames class com.fasterxml.jackson.** { *; }
-keepnames interface com.fasterxml.jackson.** { *; }

# huawei HMS ----------------------------------------------
-ignorewarning
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keep class com.huawei.hms.**{*;}
# hmscore-support: remote transport
-keep class * extends com.huawei.hms.core.aidl.IMessageEntity { *; }
# hmscore-support: remote transport
-keepclasseswithmembers class * implements com.huawei.hms.support.api.transport.DatagramTransport {
 <init>(...);
}
# manifest: provider for updates
-keep public class com.huawei.hms.update.provider.UpdateProvider { public *; protected *; }
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-keep public class com.huawei.android.hms.agent.** extends android.app.Activity { public *; protected *; }
-keep interface com.huawei.android.hms.agent.common.INoProguard {*;}
-keep class * extends com.huawei.android.hms.agent.common.INoProguard {*;}

# share sdk proguard ------------------------------------------------
-keep class cn.sharesdk.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-keep class com.mob.**{*;}
-dontwarn com.mob.**
-dontwarn cn.sharesdk.**
-dontwarn **.R$*


################xutils##################
-keep class com.lidroid.xutils.** { *; }
-keep public class * extends com.lidroid.xutils.**
-keepattributes Signature
-keepattributes *Annotation*
-keep public interface com.lidroid.xutils.** {*;}
-dontwarn com.lidroid.xutils.**
-keepclasseswithmembers class com.jph.android.entity.** {
    <fields>;
    <methods>;
}

#org.apache.http.legacy.jar
-dontwarn android.net.compatibility.**
-dontwarn android.net.http.**
-dontwarn com.android.internal.http.multipart.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.http.**
-keep class android.net.compatibility.**{*;}
-keep class android.net.http.**{*;}
-keep class com.android.internal.http.multipart.**{*;}
-keep class org.apache.commons.**{*;}
-keep class org.apache.http.**{*;}

# JPush proguard ---------------------------------------
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keepattributes Annotation
# vivo proguard ----------------------------------------
-dontwarn com.vivo.push.**
-keep class com.vivo.push.**{*; }
-keep class com.laikan.reader.core.receiver.PushMessageReceiverImpl{*;}
# gson proguard ----------------------------------------
-dontwarn com.google.**
-keep class com.google.gson.** {*;}

 #高德地图定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}


#可以防止一个误报的 warning 导致无法成功编译，如果编译使用的 Android 版本是 23。
-dontwarn com.xiaomi.push.**

##########################################################################
-keep class com.jdpaysdk.author.web.PayJsFunction {*;}
-keep class com.jdpaysdk.author.JDPayAuthor {*;}
-keep class com.jdpaysdk.author.Constants {*;}
-keep class com.jdpaysdk.author.entity.CPOrderParam {*;}



##########################################################################################

-keepclassmembers class com.jdpaysdk.author.JDPayAuthor.** {
    public *;
    private *;
}
# entity
-keepclassmembers class com.jdpaysdk.author.entity.** {
    public *;
    private *;
}
-keepclassmembers class com.jdpaysdk.author.protocol.** {
    public *;
    private *;
}

-keep class android.support.** {*;}
-dontwarn android.support.**

# end of android support
##########################################################################


##########################################################################
# annotation.

-keep public class android.annotation.** { *; }
-dontwarn android.annotation.**

# end of annotation
##########################################################################


##########################################################################
# Gson
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *;}
-dontwarn com.google.gson.**

## end of Gson
##########################################################################

##########################################################################
# google-play-service

-dontwarn com.google.**.R
-dontwarn com.google.**.R$*

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


# end of google-play-service
##########################################################################

##########################################################################

##########################################################################
# ripple

-keep class com.jdpaysdk.author.protocol.** { *; }

-keep class com.nineoldandroids.**{*;}
-dontwarn com.nineoldandroids.**

#okio
-dontwarn okio.**
-keep class okio.**{*;}
-keep interface okio.**{*;}

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}
-keep interface okhttp3.**{*;}

# end of ripple
##########################################################################

# baidu 语音合成
-keep class com.baidu.tts.**{*;}
-keep class com.baidu.speechsynthesizer.**{*;}

#腾讯bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
-dontwarn io.objectbox.**
-keep  class io.objectbox.**{*;}


-keep  class android.arch.**{*;}
-keep  interface android.arch.**{*;}
-keep  class com.alibaba.fastjson.**{*;}
-keep  interface com.alibaba.fastjson.**{*;}
-keep  class com.bumptech.glide.**{*;}
-keep  interface com.bumptech.glide.**{*;}
-keep  class com.google.flatbuffers.**{*;}
-keep  interface com.google.flatbuffers.**{*;}
-keep  class com.hjq.permissions.**{*;}
-keep  interface com.hjq.permissions.**{*;}
-keep  class butterknife.**{*;}
-keep  interface butterknife.**{*;}
-keep  class com.nostra13.universalimageloader.**{*;}
-keep  interface com.nostra13.universalimageloader.**{*;}
-keep  class com.serenegiant.**{*;}
-keep  interface com.serenegiant.**{*;}
-keep  class com.squareup.**{*;}
-keep  interface com.squareup.**{*;}
-keep  class com.zhy.http.okhttp.**{*;}
-keep  interface com.zhy.http.okhttp.**{*;}
-keep  class io.fotoapparat.**{*;}
-keep  interface io.fotoapparat.**{*;}
-keep  class io.objectbox.**{*;}
-keep  interface io.objectbox.**{*;}
-keep  class net.gotev.uploadservice.**{*;}
-keep  interface net.gotev.uploadservice.**{*;}
-keep  class net.gotev.uploadservice.okhttp.**{*;}
-keep  interface net.gotev.uploadservice.okhttp.**{*;}
-keep  class org.xmlpull.v1.**{*;}
-keep  interface org.xmlpull.v1.**{*;}
-keep  class org.kxml2.**{*;}
-keep  interface org.kxml2.**{*;}
-keep  class org.greenrobot.essentials.**{*;}
-keep  interface org.greenrobot.essentials.**{*;}
-keep  class de.greenrobot.dao.**{*;}
-keep  interface de.greenrobot.dao.**{*;}
-keep  class org.hamcrest.**{*;}
-keep  interface org.hamcrest.**{*;}
-keep  class kotlinx.coroutines.experimental.**{*;}
-keep  interface kotlinx.coroutines.experimental.**{*;}
-keep  class org.jetbrains.annotations.**{*;}
-keep  interface org.jetbrains.annotations.**{*;}
-keep  class org.intellij.lang.annotations.**{*;}
-keep  interface org.intellij.lang.annotations.**{*;}
-keep  class com.innovation.pig.insurance.netutils.Constants{*;}
-keep  class com.chad.library.**{*;}
-keep  interface com.chad.library.**{*;}
-keep  class com.yanzhenjie.loading.**{*;}
-keep  interface com.yanzhenjie.loading.**{*;}
-keep  class com.yanzhenjie.recyclerview.swipe.**{*;}
-keep  interface com.yanzhenjie.recyclerview.swipe.**{*;}
-keep  class android.support.v7.widget.helper.**{*;}
-keep  interface android.support.v7.widget.helper.**{*;}
-keep  class cn.bingoogolapple.bgabanner.**{*;}
-keep  interface cn.bingoogolapple.bgabanner.**{*;}
-keep  class cn.bingoogolapple.bgabanner.BGABanner

#-keep  class com.felipecsl.gifimageview.library.**{*;}
#-keep  interface com.felipecsl.gifimageview.library.**{*;}
#-keep  class at.wirecube.additive_animations.**{*;}
#-keep  interface at.wirecube.additive_animations.**{*;}
#-keep  class at.wirecube.additiveanimations.**{*;}
#-keep  interface at.wirecube.additiveanimations.**{*;}
-keep  class es.dmoral.prefs.**{*;}
-keep  interface es.dmoral.prefs.**{*;}
-keep  class org.greenrobot.eventbus.**{*;}
-keep  interface org.greenrobot.eventbus.**{*;}
-keep  class android.support.media.**{*;}
-keep  interface android.support.media.**{*;}
-keep public class innovation.utils.InnovationAiOpen {
    public <methods>;
    pubilc <fields>;
}