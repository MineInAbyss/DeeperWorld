import Com_mineinabyss_conventions_platform_gradle.Deps

val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.slimjar")
    id("com.mineinabyss.conventions.copyjar")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
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
    maven("https://mvn.intellectualsites.com/content/repositories/releases/") // FAWE
    maven("https://jitpack.io")
}

dependencies {
    // Plugin APIs
    compileOnly("com.fastasyncworldedit:FAWE-Bukkit:1.17-47") { isTransitive = false }
    compileOnly("com.fastasyncworldedit:FAWE-Core:1.17-47")
    compileOnly("nl.rutgerkok:blocklocker:1.9.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0")

    // Download at runtime
    slim(kotlin("stdlib-jdk8"))
    slim(Deps.kotlinx.serialization.json)
    slim(Deps.kotlinx.serialization.kaml)
    slim(Deps.minecraft.skedule)

    // Shaded
    implementation("com.mineinabyss:idofront:$idofrontVersion")
}
