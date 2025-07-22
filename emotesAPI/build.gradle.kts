plugins {
    java
    `java-library`
    `maven-publish`
}

version = mod_version

dependencies {
    api("com.zigythebird.playeranim:PlayerAnimationLibCore:${properties["playeranimlib_version"] as String}")
    api("net.raphimc:NoteBlockLib:${properties["noteblocklib_version"] as String}")
    implementation("com.google.code.gson:gson:2.11.0") // gson for MC 1.21.4
    api("org.jetbrains:annotations:24.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.3")
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