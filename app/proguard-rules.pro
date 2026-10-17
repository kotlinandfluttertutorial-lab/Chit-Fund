# Hilt keep rules
-keep class * implements dagger.hilt.internal.GeneratedEntryPoint
-keep class * implements dagger.hilt.internal.ComponentEntryPoint
-keep class * implements dagger.hilt.internal.TestSingletonComponent
-keep class com.chitsmart.**_GeneratedInjector { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Retain annotations for dependency injection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
