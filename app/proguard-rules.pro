# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.rws.kittylauncher.**$$serializer { *; }
-keepclassmembers class com.rws.kittylauncher.** {
    *** Companion;
}
-keepclasseswithmembers class com.rws.kittylauncher.** {
    kotlinx.serialization.KSerializer serializer(...);
}
