plugins { id("com.android.application") version "9.4.0" }

android {
    namespace = "com.rsgkh.calendar.engine.verification"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.rsgkh.calendar.engine.verification"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.rsgkh:khmer-calendar-engine-jvm:0.1.0")
    testImplementation("junit:junit:4.13.2")
}
