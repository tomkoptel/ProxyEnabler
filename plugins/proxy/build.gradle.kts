plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(configurations.compileOnly)
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(gradleApi())

    compileOnly("com.android.tools.build:gradle:4.1.2")

    testImplementation(gradleTestKit())
    testImplementation("commons-io:commons-io:2.8.0")
    testImplementation("junit:junit:4.13.2")
}

gradlePlugin {
    plugins {
        val pluginId = "$group"
        create(pluginId) {
            id = pluginId
            implementationClass = "$pluginId.AndroidNetworkSecurityPlugin"
            version = project.version
        }
    }
}

