plugins {
    java
    `java-library`
    `maven-publish`
}

version = mod_version

dependencies {
    api("com.zigythebird.playeranim:PlayerAnimationLibCore:${project["playeranimlib_version"]}")
    api("io.github.jaredmdobson:concentus:${project["concentus_version"]}")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
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