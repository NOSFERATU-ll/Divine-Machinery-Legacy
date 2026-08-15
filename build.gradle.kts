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
}

dependencies {
    // Exact pack jars. Keep them local and out of Git.
    api(rfg.deobf(project.files("libs/Botania r1.8-249.jar")))
    api(rfg.deobf(project.files("libs/appliedenergistics2-rv3-beta-6.jar")))
}
