plugins {
    java
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-io:commons-io:2.22.0")
}

kotlin {
    jvmToolchain(JavaVersion.current().majorVersion.toInt())
}
