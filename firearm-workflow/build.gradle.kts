apply(from = "../dsl/android-library.gradle")
apply(from = "../dsl/ktlint.gradle")
apply(from = "../dsl/bintray.gradle")

dependencies {
    /**
     * Core architexture
     */
    "api"("io.reactivex.rxjava2:rxkotlin:2.4.0")  // Reactive Extension
    "api"("io.reactivex.rxjava2:rxandroid:2.1.1")   // Reactive Extension
    "api"("androidx.activity:activity:1.1.0")
    "api"("androidx.activity:activity-ktx:1.1.0")
    "api"("androidx.annotation:annotation:1.1.0")
    "api"("androidx.appcompat:appcompat:1.1.0")
    "api"("androidx.appcompat:appcompat-resources:1.1.0")
    "api"("androidx.arch.core:core-common:2.1.0")
    "api"("androidx.arch.core:core-runtime:2.1.0")
    "api"("androidx.collection:collection:1.1.0")
    "api"("androidx.collection:collection-ktx:1.1.0")
    "api"("androidx.core:core:1.2.0")
    "api"("androidx.core:core-ktx:1.2.0")
    "api"("androidx.fragment:fragment:1.2.1")
    "api"("androidx.fragment:fragment-ktx:1.2.1")
    "api"("androidx.lifecycle:lifecycle-extensions:2.2.0")
    "api"("androidx.lifecycle:lifecycle-viewmodel:2.2.0")
    "api"("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.2.0")
    "api"("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    "api"("androidx.lifecycle:lifecycle-runtime:2.2.0")
    "api"("androidx.lifecycle:lifecycle-common-java8:2.2.0")
    "api"("androidx.lifecycle:lifecycle-reactivestreams:2.2.0")
    "api"("androidx.lifecycle:lifecycle-reactivestreams-ktx:2.2.0")
    "api"("androidx.savedstate:savedstate:1.0.0")

    /**
     * Libraries.
     */
    "api"("com.eaglesakura.armyknife.armyknife-jetpack:armyknife-jetpack:1.4.8")
}