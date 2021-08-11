val idofrontVersion: String by project

plugins {
    id("com.mineinabyss.conventions.kotlin")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.publication")
    id("com.mineinabyss.conventions.testing")
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
    compileOnly ("com.mineinabyss:geary-platform-papermc:0.6.48")

    // Download at runtime
    slim(kotlin("stdlib-jdk8"))
    slim("org.jetbrains.kotlinx:kotlinx-serialization-json")
    slim("com.github.okkero:skedule")
    slim("com.charleskorn.kaml:kaml")

    // Shaded
    implementation("com.mineinabyss:idofront:$idofrontVersion")
}
