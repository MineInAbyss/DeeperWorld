pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
    }
    plugins {
        val kotlinVersion: String by settings
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion
        id("com.github.johnrengelman.shadow") version "6.0.0"
        id("io.github.0ffz.github-packages") version "1.2.0"
    }
}

rootProject.name = "deeperworld"
