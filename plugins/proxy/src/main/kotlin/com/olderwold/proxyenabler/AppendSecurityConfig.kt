package com.olderwold.proxyenabler

import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.build.gradle.tasks.ProcessApplicationManifest
import com.android.build.gradle.tasks.ProcessMultiApkApplicationManifest
import groovy.util.Node
import groovy.util.NodeList
import groovy.util.XmlParser
import groovy.xml.XmlUtil
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.io.FileOutputStream

abstract class AppendSecurityConfig : DefaultTask() {
    private val xmlParser = XmlParser(false, false)

    private companion object {
        const val NETWORK_SECURITY_CONFIG = "android:networkSecurityConfig"
    }

    @get:Internal
    abstract val manifestProvider: Property<TaskProvider<ManifestProcessorTask>>

    @TaskAction
    fun append() {
        val task = manifestProvider.get().get()
        manifestFile(task)?.get()?.asFile?.let(::appendConfigToManifest)
    }

    private fun manifestFile(task: ManifestProcessorTask): Provider<RegularFile>? = when {
        (task is ProcessMultiApkApplicationManifest) -> {
            task.multiApkManifestOutputDirectory.file("AndroidManifest.xml")
        }
        (task is ProcessApplicationManifest) -> {
            task.mergedManifest
        }
        else -> null
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
                applicationAttributes[NETWORK_SECURITY_CONFIG] = "@xml/${GenerateSecurityConfig.FILE_NAME}"
                xml
            }
        }
    }
}
