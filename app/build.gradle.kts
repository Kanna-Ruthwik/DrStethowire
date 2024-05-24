plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.drstethowire"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.drstethowire"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    implementation ("com.itextpdf:itextpdf:5.5.13.2")
    implementation ("com.jjoe64:graphview:4.2.2")
    implementation("androidx.appcompat:appcompat:1.6.0")  // Use consistent version 1.6.0 with androidx.activity
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4-alpha05")  // Use 1.1.4-alpha05 compatible with androidx.activity:1.6.0
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0-alpha05") // Use 3.5.0-alpha05 compatible with androidx.test:1.4.1
}