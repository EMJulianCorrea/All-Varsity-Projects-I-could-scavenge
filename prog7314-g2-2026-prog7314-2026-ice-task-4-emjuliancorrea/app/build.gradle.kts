import org.gradle.accessors.dm.LibrariesForLibsInPluginsBlock

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    /*
     * Reads MAPS_API_KEY from local.properties.
     */
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.googlemapsdemo"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.googlemapsdemo"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true

        /*
         * Generates the BuildConfig class.
         *
         * The application will later use BuildConfig.MAPS_API_KEY
         * when initialising the Places SDK.
         */
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    /*
     * Provides the Google Map composables.
     *
     * Important classes include:
     * GoogleMap
     * Marker
     * MarkerState
     * MapProperties
     * CameraPositionState
     */
    implementation(
        "com.google.maps.android:maps-compose:6.12.0"
    )

    /*
     * Provides the Fused Location Provider.
     *
     * This is used to retrieve the user's current device location.
     */
    implementation(
        "com.google.android.gms:play-services-location:21.3.0"
    )

    /*
     * Provides the Places SDK.
     *
     * This is used to search for addresses, businesses,
     * landmarks and other named locations.
     */
    implementation(
        "com.google.android.libraries.places:places:5.1.1"
    )
}
/*
 * Tells the Secrets Gradle Plugin where to find
 * the real API key and the fallback value.
 */
secrets {

    /*
     * Contains the real API key.
     */
    propertiesFileName = "local.properties"

    /*
     * Contains the fallback key.
     */
    defaultPropertiesFileName =
        "local.defaults.properties"

    /*
     * Prevent normal Gradle properties from being treated as secrets.
     */
    ignoreList.add("keyToIgnore")
    ignoreList.add("sdk.*")
}