// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // оставляем включённым
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CoinSpirit2"
include(":app")
