package com.olderwold.proxyenabler

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.tasks.ProcessMultiApkApplicationManifest
import groovy.util.Node
import groovy.util.NodeList
import groovy.util.XmlParser
import groovy.xml.XmlUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.set

@Suppress("DefaultLocale")
abstract class AndroidNetworkSecurityPlugin : Plugin<Project> {
    private val xmlParser = XmlParser(false, false)

    internal companion object {
        private const val NETWORK_SECURITY_CONFIG = "android:networkSecurityConfig"

        fun taskName(flavor: String): String {
            return "generate${flavor}SecurityConfig"
        }

        fun deleteName(flavor: String): String {
            return "delete${flavor}SecurityConfig"
        }
    }

    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.android.application") {
            val projectAllowHttpProxy: String? by project
            val allowHttpProxyValue = projectAllowHttpProxy ?: "false"
            val allowHttpProxyEnv = allowHttpProxyValue.toBoolean()

            project.the<AppExtension>().applicationVariants.all {
                val generatedRes = project.layout.buildDirectory.dir("generated/res/networkSecurity").get()

                if (allowHttpProxyEnv) {
                    setupProxy(project, generatedRes)
                } else {
                    cleanupProxySetup(project, generatedRes)
                }
            }
        }
    }

    private fun ApplicationVariant.cleanupProxySetup(
        project: Project,
        generatedRes: Directory
    ) {
        val deleteSecurityConfig = project.tasks.register<Delete>(
            name = deleteName(name.capitalize())
        ) {
            setDelete(generatedRes)
        }
        mergeResourcesProvider.configure { dependsOn(deleteSecurityConfig) }
    }

    @Suppress("DefaultLocale")
    private fun ApplicationVariant.setupProxy(
        project: Project,
        generatedRes: Directory
    ) {
        val flavor = flavorName
        val buildType = buildType.name
        val flavorDir = generatedRes.dir("./$flavor/$buildType")
        val flavorDirRes = generatedRes.dir("./$flavor/$buildType/res")

        val generateSecurityConfig = project.tasks.register<GenerateSecurityConfig>(
            name = taskName(name.capitalize())
        ) {
            outputDirectory.set(flavorDir)

            // Marks the output directory as an app resource folder
            val resFolders = project.files(flavorDirRes).builtBy(this)
            registerGeneratedResFolders(resFolders)
        }
        mergeResourcesProvider.configure { dependsOn(generateSecurityConfig) }

        outputs.all {
            processManifestProvider.configure {
                doLast {
                    (this as? ProcessMultiApkApplicationManifest)?.modifyManifest(project)
                }
            }
        }
    }

    private fun ProcessMultiApkApplicationManifest.modifyManifest(project: Project) {
        val outputDirProp = multiApkManifestOutputDirectory.get()
        val manifestOutFile = project.file("${outputDirProp}/AndroidManifest.xml")
        appendConfigToManifest(manifestOutFile)
    }

    private fun appendConfigToManifest(manifestOutFile: File) {
        val manifestFileValid = manifestOutFile.exists() &&
                manifestOutFile.canRead() &&
                manifestOutFile.canWrite()

        if (manifestFileValid) {
            createModifiedAndroidManifest(manifestOutFile)?.let {
                XmlUtil.serialize(it, FileOutputStream(manifestOutFile))
            }
        }
    }

    private fun createModifiedAndroidManifest(manifestOutFile: File): Node? {
        val xml = xmlParser.parse(manifestOutFile)
        val application = (xml.get("application") as? NodeList)?.get(0) as? Node

        return if (application == null) {
            null
        } else {
            val applicationAttributes = application.attributes()
            val hasNetworkConfig = applicationAttributes.containsKey(NETWORK_SECURITY_CONFIG)

            if (hasNetworkConfig) {
                null
            } else {
                applicationAttributes[NETWORK_SECURITY_CONFIG] =
                    "@xml/${GenerateSecurityConfig.FILE_NAME}"
                xml
            }
        }
    }
}
