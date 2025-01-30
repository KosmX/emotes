architectury {
    common("fabric", "neoforge")
}

loom {
    accessWidenerPath = file("src/main/resources/emotes.accesswidener")
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${fabric_loader_version}")

    implementation(project(":emotesAssets"))
    implementation(project(":emotesAPI"))
    implementation(project(":executor"))
    implementation(project(":emotesServer"))
    api(project(path = ":emotesMc", configuration = "namedElements"))

    modApi("dev.kosmx.player-anim:player-animation-lib:${player_animator_version}")
    modImplementation("dev.kosmx.player-anim:anim-core:${player_animator_version}")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "archCommon"

            artifact(tasks.jar) {
                classifier = ""
            }
            artifact(tasks.sourcesJar)

            addDeps(project, configurations.api.get(), "compile")
            addDeps(project, configurations.modApi.get(), "compile")

            withCustomPom("archCommon", "Minecraft Emotecraft Architectury common module")
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
