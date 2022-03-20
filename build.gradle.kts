val idofrontVersion: String by project

plugins {
    alias(libs.plugins.mia.kotlin)
    alias(libs.plugins.mia.papermc)
    alias(libs.plugins.mia.copyjar)
    alias(libs.plugins.mia.publication)
    alias(libs.plugins.mia.testing)
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://www.rutgerkok.nl/repo")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.mineinabyss.com/releases/")
    maven("https://papermc.io/repo/repository/maven-public/") //Paper
    maven("https://repo.codemc.org/repository/maven-public/")
    //maven("https://mvn.intellectualsites.com/content/repositories/releases/") // FAWE
    maven("https://jitpack.io")
}

dependencies {
    // MineInAbyss platform
    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlinx.serialization.json)
    compileOnly(libs.kotlinx.serialization.kaml)
    compileOnly(libs.minecraft.skedule)

    // Plugin APIs
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.0.1")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.0.1") { isTransitive = false }
    compileOnly("nl.rutgerkok:blocklocker:1.10.4")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0-SNAPSHOT")

    // Shaded
    implementation("com.mineinabyss:idofront:$idofrontVersion")
}
