package com.olderwold.proxyenabler

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File


class AndroidNetworkSecuritySmokePluginTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Before
    fun setupProject() {
        testProjectDir.newFile("build.gradle")
            .writeText(
                """
            buildscript {
                repositories {
                    google()
                    mavenCentral()
                }
                dependencies {
                    classpath("com.android.tools.build:gradle:4.1.2")
                }
            }
            plugins {
                id 'com.android.application'
            }
            repositories {
                google()
                mavenCentral()
            }
            android {
                compileSdkVersion(30)
                buildToolsVersion("30.0.2")

                defaultConfig {
                    applicationId = "com.olderwold.dummy"

                    minSdkVersion(23)
                    targetSdkVersion(30)
                    versionCode = 1
                    versionName = "1.0"
                }
            }
            dependencies {
                implementation 'androidx.appcompat:appcompat:1.2.0'
                implementation 'com.google.android.material:material:1.3.0'
            }
            """.trimIndent()
            )
        testProjectDir.newFile("gradle.properties")
            .writeText(
                """
            org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
            android.useAndroidX=true
            android.enableJetifier=false
            kotlin.code.style=official
            """.trimIndent()
            )
        val classLoader = javaClass.classLoader
        val srcDir = File(classLoader.getResource("src").file)
        val destDir = File(testProjectDir.root, "src")
        destDir.mkdirs()

        FileUtils.copyDirectory(srcDir, destDir)
    }

    @Test
    fun `test task with allow proxy env`() {
        val result = build(task = "assembleDebug", args = listOf("-PprojectAllowHttpProxy=true"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":assembleDebug")?.outcome)
    }

    private fun build(task: String, args: List<String>): BuildResult {
        val mergedArgs = listOf(listOf(task), args).flatten()
        return GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withTestKitDir(testProjectDir.newFolder())
            .withArguments(mergedArgs)
            .withPluginClasspath()
            .withDebug(true)
            .build()
    }
}
