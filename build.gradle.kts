plugins {
    `java-library`
    `maven-publish`
    eclipse
    idea
    id("com.gtnewhorizons.retrofuturagradle") version "2.0.2"
}

group = "com.nosferatu.divinemachinerylegacy"
version = "0.1.0-dev"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withSourcesJar()
}

minecraft {
    mcVersion.set("1.7.10")
    username.set("Developer")
}

tasks.processResources.configure {
    inputs.property("version", project.version)
    filesMatching("mcmod.info") {
        expand(mapOf("version" to project.version.toString()))
    }
}

repositories {
    mavenCentral()
    maven {
        name = "GTNH Maven"
        url = uri("https://nexus.gtnewhorizons.com/repository/public/")
    }
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    // Prefer the exact jars from the user's Divine Journey instance when they
    // are present. CI and clean clones fall back to the exact same CurseForge
    // file IDs, so the target versions do not silently drift.
    val localBotania = file("libs/Botania r1.8-249.jar")
    val localAe2 = file("libs/appliedenergistics2-rv3-beta-6.jar")

    if (localBotania.exists()) {
        api(rfg.deobf(files(localBotania)))
    } else {
        api(rfg.deobf("curse.maven:botania-225643:2283837"))
    }

    if (localAe2.exists()) {
        api(rfg.deobf(files(localAe2)))
    } else {
        api(rfg.deobf("curse.maven:applied-energistics-2-223794:2296430"))
    }
}
