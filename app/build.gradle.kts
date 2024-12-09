plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}


android {
    namespace = "com.example.pintapiconv3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pintapiconv3"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/ASL2.0")
            excludes.add("META-INF/NOTICE.md")
            excludes.add("META-INF/LICENSE.md")
        }
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(files("../libs/jtds-1.3.1.jar"))
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.runtime.saved.instance.state)
    implementation(libs.testng)
    implementation(libs.play.services.location)
    implementation(libs.androidx.ui.desktop)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.lifecycle.viewmodel.ktx) // ViewModel
    implementation(libs.mssql.jdbc) // SQL Server
    implementation(libs.android.mail)
    implementation(libs.android.activation) // Activation
    implementation(libs.jjwt.api) // Token JWT
    implementation(libs.jjwt.impl) // *
    implementation(libs.jjwt.jackson) // *
    implementation(libs.guava)
    implementation(libs.play.services.maps) // Google Maps
    implementation(libs.places) // Google Places
    implementation(libs.androidx.fragment.ktx) // viewModels
    implementation(libs.philjay.mpandroidchart) // Grafico de torta
}

