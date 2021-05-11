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
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://www.rutgerkok.nl/repo")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.mineinabyss.com/releases")
    maven("https://papermc.io/repo/repository/maven-public/") //Paper
    maven("https://repo.codemc.org/repository/maven-public/")
//    maven("https://mvn.intellectualsites.com/content/repositories/releases/") // FAWE
    maven("https://jitpack.io")
}

val serverVersion: String by project
val kotlinVersion: String by project

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:$serverVersion")
//    compileOnly("com.intellectualsites.fawe:FAWE-Bukkit:1.16-637")
    compileOnly(kotlin("stdlib-jdk8"))

    kotlinSpice("$kotlinVersion+")
    compileOnly("com.github.okkero:skedule")

    compileOnly("nl.rutgerkok:blocklocker:1.9.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.5.0")

    implementation("com.mineinabyss:idofront:0.6.13")

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
