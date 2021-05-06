import com.mineinabyss.kotlinSpice
import com.mineinabyss.sharedSetup

plugins {
    java
    idea
    `maven-publish`
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("com.mineinabyss.shared-gradle") version "0.0.6"
}

sharedSetup()

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://www.rutgerkok.nl/repo")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.mineinabyss.com/releases")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://jitpack.io")
}

val serverVersion: String by project
val kotlinVersion: String by project

dependencies {
    compileOnly("org.spigotmc:spigot-api:$serverVersion")
    compileOnly(kotlin("stdlib-jdk8"))

    kotlinSpice("$kotlinVersion+")
    compileOnly("com.github.okkero:skedule")

    compileOnly("nl.rutgerkok:blocklocker:1.9.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0")

    implementation("com.mineinabyss:idofront:0.5.8")

    testImplementation("junit:junit:4.12")
}

tasks {
    shadowJar {
        relocate("com.mineinabyss.idofront", "${project.group}.${project.name}.idofront".toLowerCase())

        minimize()
    }

    build {
        dependsOn(shadowJar)
    }
}

publishing {
    mineInAbyss(project) {
        from(components["java"])
    }
}