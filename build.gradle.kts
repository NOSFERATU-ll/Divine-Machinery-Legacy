plugins {
    // Modern build runtime, legacy Minecraft output.
    // FPGradle 4.1 runs on Java 25 while `compatibility = legacy` keeps the
    // produced mod compatible with the Java 8-era Minecraft 1.7.10 runtime.
    id("com.falsepattern.fpgradle-mc") version "4.1.0"
}

group = "com.nosferatu.divinemachinerylegacy"
version = "0.1.0-dev"

minecraft_fp {
    java {
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

repositories {
    mavenCentral()
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    // Prefer the exact jars supplied from the Divine Journey instance.
    // CI/clean clones use the exact CurseForge files for the same versions.
    val localBotania = file("libs/Botania r1.8-249.jar")
    val localAe2 = file("libs/appliedenergistics2-rv3-beta-6.jar")
    val localCoFH = file("libs/CoFHCore-[1.7.10]3.1.4-329.jar")
    val localBloodMagic = file("libs/BloodMagic-1.7.10-1.3.3-17.jar")

    if (localBotania.exists()) {
        devOnlyNonPublishable(rfg.deobf(files(localBotania)))
    } else {
        devOnlyNonPublishable(rfg.deobf("curse.maven:botania-225643:2283837"))
    }

    if (localAe2.exists()) {
        devOnlyNonPublishable(rfg.deobf(files(localAe2)))
    } else {
        devOnlyNonPublishable(rfg.deobf("curse.maven:applied-energistics-2-223794:2296430"))
    }

    if (localCoFH.exists()) {
        devOnlyNonPublishable(rfg.deobf(files(localCoFH)))
    } else {
        devOnlyNonPublishable(rfg.deobf("curse.maven:CoFHCore-69162:2388751"))
    }

    if (localBloodMagic.exists()) {
        devOnlyNonPublishable(rfg.deobf(files(localBloodMagic)))
    } else {
        devOnlyNonPublishable(rfg.deobf("curse.maven:blood-magic-224791:2264826"))
    }
}

// BloodMagic Additions patches AE2's upgrade inventory in modern versions.
// The 1.7.10 backport carries an equally small LaunchWrapper transformer in
// the same jar so the Blood Magic Speed Card works in every AE2 SPEED host.
tasks.withType<org.gradle.jvm.tasks.Jar>().configureEach {
    manifest {
        attributes(
            mapOf(
                "FMLCorePlugin" to "com.nosferatu.divinemachinerylegacy.core.DivineMachineryLegacyCorePlugin",
                "FMLCorePluginContainsFMLMod" to "true"
            )
        )
    }
}
