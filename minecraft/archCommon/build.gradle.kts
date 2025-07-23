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
    implementation(project(":emotesServer"))
    api(project(path = ":emotesMc", configuration = "namedElements"))

    modApi("com.zigythebird.playeranim:PlayerAnimationLibCommon:${properties["playeranimlib_version"] as String}")
    implementation("com.zigythebird.playeranim:PlayerAnimationLibCore:${properties["playeranimlib_version"] as String}")

    // Third-party
    compileOnly("com.blamejared.searchables:Searchables-common-${minecraft_version}:${properties["searchables_version"] as String}") {
        isTransitive = false
    }

    modCompileOnly("maven.modrinth:entity-model-features:n0dDeW4R")
    modCompileOnly("maven.modrinth:entitytexturefeatures:441qELLe")
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
