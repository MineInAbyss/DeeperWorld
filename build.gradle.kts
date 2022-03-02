import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
    id("io.papermc.paperweight.userdev") version "1.3.4"
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://www.rutgerkok.nl/repo")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.mineinabyss.com/releases")
    maven("https://papermc.io/repo/repository/maven-public/") //Paper
    maven("https://repo.codemc.org/repository/maven-public/")
    //maven("https://mvn.intellectualsites.com/content/repositories/releases/") // FAWE
    maven("https://jitpack.io")
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    // MineInAbyss platform
    compileOnly(Deps.kotlin.stdlib)
    compileOnly(Deps.kotlinx.serialization.json)
    compileOnly(Deps.kotlinx.serialization.kaml)
    compileOnly(Deps.minecraft.skedule)
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")

    // Plugin APIs
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.0.1")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.0.1") { isTransitive = false }
    compileOnly("nl.rutgerkok:blocklocker:1.10.4")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0-SNAPSHOT")

    // Shaded
    implementation("com.mineinabyss:idofront:$idofrontVersion")
}
