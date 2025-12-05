import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 36
    buildToolsVersion = "36.1.0"
    val appName = "Capacity Info"
    defaultConfig {
        applicationId = "com.ph03nix_x.capacityinfo"
        namespace = applicationId
        minSdk = 26
        targetSdk = 36
        versionCode = 1772
        versionName = "9.2.3.1"
        setProperty("archivesBaseName", "$appName $versionName (Build $versionCode)")
        buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        localeFilters += listOf("en", "cs", "de", "es", "fr", "in", "it", "pl", "pt", "ro", "el",
            "be", "bg", "kk", "ru", "uk")
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        }
    }
    buildTypes {
        getByName("release") {
            buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}
fun getBuildDate(): String {
    val date = Date.from(Calendar.getInstance().toInstant())
    val formatter = SimpleDateFormat("dd.MM.yyy HH:mm")
    return formatter.format(date)
}
dependencies {
    val kotlinVersion = rootProject.extra.get("kotlin_version") as String
    val coroutinesVersion = "1.10.2"
    val adMobVersion = "24.8.0"
    val billingVersion = "8.1.0"
    val miuiAutoStart = "v1.3"

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.documentfile:documentfile:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Ads
    implementation("com.google.android.gms:play-services-ads:$adMobVersion")

    // Premium
    implementation("com.android.billingclient:billing-ktx:$billingVersion")

    // Checking Autostart on Xiaomi
    implementation("com.github.XomaDev:MIUI-autostart:$miuiAutoStart")

    // Color Picker
    implementation("com.jaredrummler:colorpicker:1.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}