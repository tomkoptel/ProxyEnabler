plugins {
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.10.0"
}

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.2")
    }
}

allprojects {
    group = "com.olderwold.sliide"
    version = "1.0"

    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    apply {
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    ktlint {
        debug.set(false)
        version.set("0.40.0")
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(true)
        enableExperimentalRules.set(true)
        filter {
            exclude { projectDir.toURI().relativize(it.file.toURI()).path.contains("/generated/") }
            include("**/kotlin/**")
        }
    }

    detekt {
        reports {
            html {
                enabled = true
                destination = file("build/reports/detekt.html")
            }
        }
    }

    val ktlint = tasks.withType<org.jlleitschuh.gradle.ktlint.KtlintCheckTask>()
    val detekt = tasks.withType<io.gitlab.arturbosch.detekt.Detekt>()
    tasks.matching { it.name.contains("check") }
        .configureEach {
            this.dependsOn(ktlint)
            this.dependsOn(detekt)
        }

    detekt.configureEach {
        exclude { projectDir.toURI().relativize(it.file.toURI()).path.contains("/generated/") }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
