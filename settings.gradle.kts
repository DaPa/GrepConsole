import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

pluginManagement {
    plugins {
        // used by build.gradle.kts
        id("org.jetbrains.kotlin.jvm") version "2.4.10"  // https://plugins.gradle.org/plugin/org.jetbrains.kotlin.jvm
        id("org.jetbrains.changelog") version "2.5.0"  // https://plugins.gradle.org/plugin/org.jetbrains.changelog
        id("org.jetbrains.intellij.platform") version "2.18.1"  // https://plugins.gradle.org/plugin/org.jetbrains.intellij.platform
    }
}

plugins {
    // https://plugins.gradle.org/search?term=org.jetbrains.intellij.platform.settings
    id("org.jetbrains.intellij.platform.settings") version "2.18.1"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS
    repositories {
        mavenCentral()
        // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
        intellijPlatform {
            defaultRepositories()
            jetbrainsRuntime()
        }
    }
}

rootProject.name = "GrepConsole"
include("http-client")
