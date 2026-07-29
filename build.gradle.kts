import org.jetbrains.changelog.Changelog

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)


plugins {
    id("java") // Java support
    id("org.jetbrains.changelog")
    id("org.jetbrains.intellij.platform")
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

//// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
//kotlin {
//    jvmToolchain(17)
//}


dependencies {
    intellijPlatform {
        intellijIdea("2026.2")
        jetbrainsRuntime()

        // Expose the Java compiler and build server classes to your classpath. Fixes:
        //  com.intellij.compiler.server.BuildManager.ALLOW_AUTOMAKE
        //  in krasa/grepconsole/action/TailFileInConsoleAction.java
        bundledModule("intellij.java.compiler.impl")

        // Add the explicit bundled module for the SM test runner. Fixes: SMTestRunnerResultsForm
        //  in krasa/grepconsole/grep/actions/OpenGrepConsoleAction.java
        bundledModule("intellij.platform.smRunner")

        // Fixes: BaseTestsOutputConsoleView & TestResultsPanel
        //  in krasa/grepconsole/grep/actions/OpenGrepConsoleAction.java
        bundledModule("intellij.platform.testRunner")

        // Pulls in Java execution configurations, JavaParameters, and SDK layers
        bundledPlugin("com.intellij.java") // Needed for compilation only

        // Fixes: RunConfigurationExtension, JavaRunConfigurationExtensionManager
        //  in krasa/grepconsole/plugin/runConfiguration/GrepRunConfigurationExtensionNew.java
        bundledModule("intellij.java.execution.impl")
    }

    // JUnit 4 tests
    testImplementation("junit:junit:4.13.2")

    // JUnit 5 tests
    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.18.0")

    implementation("com.github.albfernandez:juniversalchardet:2.4.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.jctools:jctools-core:4.0.1")
    implementation("commons-beanutils:commons-beanutils:1.11.0")
    implementation("uk.com.robust-it:cloning:1.9.12")
    implementation(project(":http-client"))
}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }

    buildSearchableOptions {
        enabled = false
    }
    compileJava {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }
}

// Configure Gradle IntelliJ Platform Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }

    publishing {
        token = environment("PUBLISH_TOKEN")
    }

    signing {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
//qodana {
//    cachePath = provider { file(".qodana").canonicalPath }
//    reportPath = provider { file("build/reports/inspections").canonicalPath }
//    saveReport = true
//    showReport = environment("QODANA_SHOW_REPORT").map { it.toBoolean() }.getOrElse(false)
//}
//
//// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
//kover.xmlReport {
//    onCheck = true
//}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    patchPluginXml {
//        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
//        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
//            val start = "<!-- Plugin description -->"
//            val end = "<!-- Plugin description end -->"
//
//            with (it.lines()) {
//                if (!containsAll(listOf(start, end))) {
//                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
//                }
//                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
//            }
//        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased()).withHeader(false).withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    publishPlugin {
        dependsOn("patchChangelog")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
//        channels = properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
    }

}

