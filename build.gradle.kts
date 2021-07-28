plugins {
    id("com.mineinabyss.conventions.kotlin")
    kotlin("plugin.serialization")
    id("com.mineinabyss.conventions.papermc")
    id("com.mineinabyss.conventions.publication")
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
    // Kotlin spice dependencies
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json")
    // Shaded
    implementation("com.mineinabyss:idofront:1.17.1-0.6.22")
    // Plugin APIs
    compileOnly("com.fastasyncworldedit:FAWE-Bukkit:1.17-47") { isTransitive = false }
    compileOnly("com.fastasyncworldedit:FAWE-Core:1.17-47")
    compileOnly("nl.rutgerkok:blocklocker:1.9.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0")

    // Other
    compileOnly("com.github.okkero:skedule")
    compileOnly("com.charleskorn.kaml:kaml")

    testImplementation("junit:junit:4.12")
}
