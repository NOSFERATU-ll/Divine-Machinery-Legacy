plugins {
    // FPGradle 4.x moved the Gradle runtime baseline forward. 3.3.1 is the
    // maintained pre-4.x line and uses the Java 17-era toolchain we need here.
    id("com.falsepattern.fpgradle-mc") version "3.3.1"
}

group = "com.nosferatu.divinemachinerylegacy"
version = "0.1.0-dev"

minecraft_fp {
    java {
        // Keep the produced mod compatible with the Java 8-era 1.7.10 pack.
        compatibility = legacy
    }

    mod {
        modid = "divinemachinerylegacy"
        name = "Divine Machinery Legacy"
        version = project.version.toString()
        rootPkg = "com.nosferatu.divinemachinerylegacy"
    }

    run {
        username = "Developer"
    }

    updates {
        check = false
    }
}

tasks.processResources.configure {
    inputs.property("version", project.version)
    filesMatching("mcmod.info") {
        expand(mapOf("version" to project.version.toString()))
    }
}

repositories {
    mavenCentral()
    cursemavenEX()
}

dependencies {
    // Prefer the exact jars supplied from the Divine Journey instance.
    // CI/clean clones use the exact CurseForge files for the same versions.
    val localBotania = file("libs/Botania r1.8-249.jar")
    val localAe2 = file("libs/appliedenergistics2-rv3-beta-6.jar")

    if (localBotania.exists()) {
        devOnlyNonPublishable(rfg.deobf(files(localBotania)))
    } else {
        devOnlyNonPublishable(deobfCurse("botania-225643:2283837"))
    }

    if (localAe2.exists()) {
        devOnlyNonPublishable(rfg.deobf(files(localAe2)))
    } else {
        devOnlyNonPublishable(deobfCurse("applied-energistics-2-223794:2296430"))
    }
}
