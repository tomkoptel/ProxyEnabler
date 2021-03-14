plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// Add a source set for the functional test suite. This must come _above_ the `dependencies` block.
val functionalTestSourceSet = sourceSets.create("functionalTest") {
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(configurations.compileOnly)
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(gradleApi())

    implementation("com.android.tools.build:gradle:4.1.2")

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

