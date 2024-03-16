@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(idofrontLibs.plugins.kotlinx.serialization)
    alias(idofrontLibs.plugins.mia.kotlin.jvm)
    alias(idofrontLibs.plugins.mia.papermc)
    alias(idofrontLibs.plugins.mia.copyjar)
    alias(idofrontLibs.plugins.mia.testing)
    alias(idofrontLibs.plugins.mia.publication)
    alias(idofrontLibs.plugins.mia.autoversion)
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://www.rutgerkok.nl/repo")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.mineinabyss.com/releases/")
    maven("https://papermc.io/repo/repository/maven-public/") //Paper
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    // MineInAbyss platform
    compileOnly(idofrontLibs.kotlin.stdlib)
    compileOnly(idofrontLibs.kotlinx.serialization.json)
    compileOnly(idofrontLibs.kotlinx.serialization.kaml)
    compileOnly(idofrontLibs.kotlinx.coroutines)
    compileOnly(idofrontLibs.minecraft.mccoroutine)

    // Plugin APIs
    compileOnly(idofrontLibs.minecraft.plugin.fawe.core)
    compileOnly(idofrontLibs.minecraft.plugin.fawe.bukkit) { isTransitive = false }
    compileOnly(idofrontLibs.minecraft.plugin.protocollib)
    compileOnly(libs.minecraft.plugin.blocklocker)

    // Shaded
    implementation(idofrontLibs.bundles.idofront.core)
}
