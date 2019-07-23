## Genericsは必須
-keepattributes Signature

## enum系はJSONパース等に頻繁に使われるため、保護する
-keepclassmembers enum * { *; }

## AppCompat系は内部情報を保護する
-keepclassmembers class android.support.** { *; }
-keepclassmembers class android.arch.** { *; }

# OkHttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase