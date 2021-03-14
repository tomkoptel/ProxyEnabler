package com.olderwold.proxyenabler

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateSecurityConfig : DefaultTask() {
    internal companion object {
        const val FILE_NAME = "network_security_config"

        private val ALLOW_PROXY =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <network-security-config>
                <debug-overrides>
                    <trust-anchors>
                        <certificates src="system" />
                        <certificates src="user" />
                    </trust-anchors>
                </debug-overrides>
                <domain-config cleartextTrafficPermitted="true">
                    <domain includeSubdomains="true">localhost</domain>
                </domain-config>
            </network-security-config>
            """.trimIndent()
    }

    @get:Input
    abstract val allowHttpProxy: Property<Boolean>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val generateConfig = allowHttpProxy.get()
        val flavorDir = outputDirectory.get()

        if (generateConfig) {
            if (!flavorDir.asFile.exists()) {
                flavorDir.asFile.mkdirs()
            }

            val resXmlDir = flavorDir.dir("./res/xml")
            if (!resXmlDir.asFile.exists()) {
                resXmlDir.asFile.mkdirs()
            }
            val securityFile = resXmlDir.file("./$FILE_NAME.xml").asFile
            if (!securityFile.exists()) {
                securityFile.createNewFile()
                securityFile.writeText(ALLOW_PROXY)
            }
        } else {
            project.delete(flavorDir.asFileTree)
        }
    }
}
