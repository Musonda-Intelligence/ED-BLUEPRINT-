# Keep kotlinx.serialization generated serializers for API models.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class com.edblueprint.app.data.remote.** {
    *** Companion;
}
-keepclasseswithmembers class com.edblueprint.app.data.remote.** {
    kotlinx.serialization.KSerializer serializer(...);
}
