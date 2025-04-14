plugins {
    java
    `java-library`
    `maven-publish`
}

version = mod_version

dependencies {
    api("dev.kosmx.player-anim:anim-core:${properties["player_animator_version"] as String}")
    api("org.apache.commons:commons-lang3:3.17.0")
    implementation("com.google.code.gson:gson:2.11.0") // gson for MC 1.21.4
    api("org.jetbrains:annotations:24.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    sourceCompatibility = "21"
    targetCompatibility = "21"
    options.release.set(21) //Build on JDK 1.8
}

//-------- publishing --------

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "emotesAPI"

            from(components["java"]) // jar, sourcesJar, javadocJar

            withCustomPom("emotesApi", "Minecraft Emotecraft API")
        }
    }

    repositories {
        if (shouldPublishMaven) {
            kosmxRepo(project)
        } else {
            mavenLocal()
        }
    }
}