plugins {
    java
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.2.21"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("me.modmuss50.mod-publish-plugin:me.modmuss50.mod-publish-plugin.gradle.plugin:0.8.4")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    compileOnly("com.squareup.okhttp3:okhttp:5.3.2")

    implementation("commons-io:commons-io:2.21.0")
}
