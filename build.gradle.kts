// Top-level build file — project-wide config goes here.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Google Services plugin — required for Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
}