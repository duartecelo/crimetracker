// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
    id("org.sonarqube") version "6.0.1.5171"
}


tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

sonar {
    properties {
        property("sonar.projectKey", "marceloduarte_crimetracker_android")
        property("sonar.organization", "marceloduarte")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
