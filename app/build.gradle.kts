import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 34
    buildToolsVersion = "34.0.0"
    defaultConfig {
        applicationId = "com.ph03nix_x.capacityinfo"
        namespace = applicationId
        minSdk = 26
        targetSdk = 34
        versionCode = 1544
        versionName = "7.6.0.1"
        buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf("en", "de", "es", "fr", "in", "it", "pl", "pt", "ro", "be",
            "bg", "kk", "ru", "uk")
    }
    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "1.8"
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

        applicationVariants.all {
            val variant = this
            variant.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { output ->
                    val appName = "Capacity Info"
                    val outputFileName = "$appName ${variant.versionName} (Build " +
                            "${variant.versionCode}).apk"
                    output.outputFileName = outputFileName
                }
        }
    }
    buildFeatures {
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
    val coroutinesVersion = "1.7.3"
    val billingVersion = "6.1.0"
    val miuiAutoStart = "v1.3"

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Premium
    implementation("com.android.billingclient:billing-ktx:$billingVersion")

    // Checking Autostart on Xiaomi
    implementation("com.github.XomaDev:MIUI-autostart:$miuiAutoStart")

    // Color Picker
    implementation("com.jaredrummler:colorpicker:1.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}