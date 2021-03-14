package com.olderwold.proxyenabler

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AndroidNetworkSecuritySmokePluginTest {
    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Before
    fun setupProject() {
        val buildGradle = testProjectDir.newFile("build.gradle")
        buildGradle.writeText(
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
                id 'com.olderwold.proxyenabler'
            }
            repositories {
                google()
                mavenCentral()
            }
            android {
                compileSdkVersion(30)
                buildToolsVersion("30.0.2")

                defaultConfig {
                    applicationId = "com.dummy.android.app"

                    minSdkVersion(23)
                    targetSdkVersion(30)
                    versionCode = 1
                    versionName = "1.0"
                }
            }
            """.trimIndent()
        )

        with(testProjectDir) {
            newFolder("src", "main")
            newFile("src/main/AndroidManifest.xml")
        }.writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.dummy.android.app">
            </manifest>
            """.trimIndent()
        )
    }

    @Test
    fun `test task with allow proxy env`() {
        val result = build(task = "assembleDebug", args =  listOf("-PprojectAllowHttpProxy=true"))
        assertEquals(TaskOutcome.SUCCESS, result.task(":assembleDebug")?.outcome)
    }

    private fun build(task: String,args: List<String>): BuildResult {
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
