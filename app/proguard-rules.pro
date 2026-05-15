# ==============================================
# REGLAS PROGUARD - CONFIN2
# ==============================================

# Mantener líneas de Firebase
-keepattributes Signature
-keepattributes *Annotation*

# Firebase Auth
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Firebase Database
-keep class com.upc.confin.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Mantener modelos de datos
-keep class com.upc.confin.User { *; }
-keep class com.upc.confin.Transaction { *; }
-keep class com.upc.confin.Category { *; }
-keep class com.upc.confin.Tipo { *; }

# Evitar errores de reflexión
-keepclassmembers class * {
    @com.google.firebase.database.IgnoreExtraProperties <fields>;
}

# Para aplicaciones que usan Google Sign-In
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.games.** { *; }

# Evitar warnings
-dontwarn com.google.errorprone.annotations.*
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement