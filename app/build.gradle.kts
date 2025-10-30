plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.upc.confin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.upc.confin"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // --- CORRECCIÓN DE SINTAXIS ---
    // Todas las dependencias de 'libs' deben ir entre paréntesis.

    // --- DEPENDENCIAS EXISTENTES (CORREGIDAS) ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // (Dependencias hardcodeadas - estas ya usaban paréntesis, por eso estaban bien)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    // --- CORRECCIONES DE FIREBASE ---

    // 1. AÑADE EL BoM (Bill of Materials)
    // La sintaxis en Kotlin es `platform(...)`
    implementation(platform(libs.firebase.bom))

    // 2. AÑADE LAS DEPENDENCIAS DE FIREBASE (CON PARÉNTESIS)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.ai)
}