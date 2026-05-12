plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yiqiu.shirohaquiz"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "variant"

    productFlavors {
        create("web") {
            dimension = "variant"
            applicationId = "com.yiqiu.shirohaquiz"
            versionCode = 19
            versionName = "0.4.3-alpha"
        }
        create("native") {
            dimension = "variant"
            applicationId = "com.reqir.shirohaquiz"
            versionCode = 26
            versionName = "0.2.5"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val exportWebReleaseApk by tasks.registering(Copy::class) {
    from(layout.buildDirectory.file("outputs/apk/web/release/app-web-release.apk"))
    into(layout.buildDirectory.dir("outputs/shiroha-quiz"))
    rename { "Shiroha-Quiz-v0.4.3-alpha-web-release.apk" }
}

val exportNativeReleaseApk by tasks.registering(Copy::class) {
    from(layout.buildDirectory.file("outputs/apk/native/release/app-native-release.apk"))
    into(layout.buildDirectory.dir("outputs/shiroha-quiz"))
    rename { "Shiroha-Quiz-v0.2.5-native-release.apk" }
}

afterEvaluate {
    tasks.named("assembleWebRelease") {
        finalizedBy(exportWebReleaseApk)
    }
    tasks.named("assembleNativeRelease") {
        finalizedBy(exportNativeReleaseApk)
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation(files("E:/codex/exercise/output/gradle-8.7/lib/junit-4.13.2.jar"))
    testImplementation(files("E:/codex/exercise/output/gradle-8.7/lib/hamcrest-core-1.3.jar"))
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
