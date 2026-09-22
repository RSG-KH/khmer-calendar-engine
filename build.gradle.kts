import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

plugins {
    kotlin("multiplatform") version "2.4.20"
    `maven-publish`
}

group = "com.rsgkh"
version = "0.4.0"

// Developers install JDK 25 locally; CI provisions it with setup-java.
tasks.named<UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion.set(JavaLanguageVersion.of(25))
    toolchainDownloadUrls.empty()
}

// Use the developer/CI Node installation; runtime consumers need no Gradle or Kotlin tools.
rootProject.plugins.withType<NodeJsRootPlugin> {
    rootProject.the<NodeJsEnvSpec>().download = false
}

kotlin {
    jvmToolchain(25)
    // Keep published metadata and APIs readable by Kotlin 2.2 Android consumers.
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
    }
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
        testRuns["test"].executionTask.configure {
            val baseline = layout.buildDirectory.file("reports/engine-baseline.tsv")
            systemProperty("engine.baselineFile", baseline.get().asFile.absolutePath)
            outputs.file(baseline)
        }
    }
    js {
        outputModuleName = "khmer-calendar-engine"
        useEsModules()
        nodejs { testTask { useMocha { timeout = "120s" } } }
        binaries.library()
        generateTypeScriptDefinitions()
    }
    sourceSets {
        commonMain.dependencies { api(kotlin("stdlib", "2.2.10")) }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

publishing {
    repositories {
        maven {
            name = "localBuild"
            url = uri(layout.buildDirectory.dir("repository"))
        }
    }
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("Khmer Calendar Engine")
            description.set("Shared Khmer calendar calculations and recurrence rules")
            licenses {
                license { name.set("Apache-2.0"); url.set("https://www.apache.org/licenses/LICENSE-2.0") }
                license { name.set("MIT (adapted calendar algorithms)"); url.set("https://opensource.org/license/mit") }
            }
        }
    }
}

tasks.withType<Jar>().configureEach {
    from(listOf("LICENSE", "NOTICE")) { into("META-INF") }
}

val packageJs = tasks.register<Exec>("packageJs") {
    dependsOn("jsNodeProductionLibraryDistribution")
    inputs.dir("js")
    inputs.dir("docs")
    inputs.file("verification/README.md")
    inputs.file("tools/package-js.mjs")
    inputs.files("README.md", "LICENSE", "NOTICE", "build.gradle.kts")
    inputs.dir(layout.buildDirectory.dir("dist/js/productionLibrary"))
    outputs.dir(layout.buildDirectory.dir("npm"))
    commandLine("node", "tools/package-js.mjs")
}

val verifyJs = tasks.register<Exec>("verifyJs") {
    dependsOn("jvmTest", packageJs)
    commandLine("node", "tools/verify-js.mjs")
}
tasks.named("check") { dependsOn(verifyJs) }
