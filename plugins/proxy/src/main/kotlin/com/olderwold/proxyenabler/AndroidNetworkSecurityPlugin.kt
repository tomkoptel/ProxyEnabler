package com.olderwold.proxyenabler

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the

abstract class AndroidNetworkSecurityPlugin : Plugin<Project> {
    companion object {
        fun taskName(flavor: String): String {
            return "generate${flavor}SecurityConfig"
        }

        fun appendTaskName(flavor: String): String {
            return "append${flavor}SecurityConfig"
        }
    }

    @Suppress("DefaultLocale")
    override fun apply(project: Project) = project.run {
        pluginManager.withPlugin("com.android.application") {
            val projectAllowHttpProxy: String? by project
            val allowHttpProxyValue = projectAllowHttpProxy ?: "false"
            val allowHttpProxyEnv = allowHttpProxyValue.toBoolean()

            the<AppExtension>().applicationVariants.all {
                val variant: ApplicationVariant = this
                val generatedRes = project.layout.buildDirectory.dir("generated/res/networkSecurity").get()
                val flavor = variant.flavorName
                val buildType = variant.buildType.name
                val flavorDir = generatedRes.dir("./$flavor/$buildType")
                val flavorDirRes = generatedRes.dir("./$flavor/$buildType/res")

                val generateSecurityConfig = tasks.register<GenerateSecurityConfig>(
                    name = taskName(name.capitalize())
                ) {
                    outputDirectory.set(flavorDir)
                    allowHttpProxy.set(allowHttpProxyEnv)

                    // Marks the output directory as an app resource folder
                    val resFolders = project.files(flavorDirRes).builtBy(this)
                    variant.registerGeneratedResFolders(resFolders)
                }
                variant.mergeResourcesProvider.configure { dependsOn(generateSecurityConfig) }

                variant.outputs.all {
                    val appendConfig = tasks.register<AppendSecurityConfig>(
                        name = appendTaskName(name.capitalize())
                    ) {
                        manifestProvider.set(processManifestProvider)
                    }
                    processManifestProvider.configure {
                        dependsOn(generateSecurityConfig)
                        finalizedBy(appendConfig)
                    }
                }
            }
        }
    }
}
