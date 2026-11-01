import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.OP
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission.Default.TRUE
import net.minecrell.pluginyml.paper.PaperPluginDescription.RelativeLoadOrder.BEFORE

plugins {
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(miaLibs.plugins.mia.kotlin.jvm)
    alias(miaLibs.plugins.mia.papermc)
    alias(miaLibs.plugins.mia.copyjar)
    alias(miaLibs.plugins.mia.testing)
    alias(miaLibs.plugins.mia.nms)
    alias(miaLibs.plugins.mia.publication)
    alias(miaLibs.plugins.mia.autoversion)
    alias(miaLibs.plugins.mia.docs)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
repositories {
    mavenCentral()
    maven("https://repo.mineinabyss.com/releases/")
    maven("https://repo.mineinabyss.com/snapshots")
    maven("https://repo.mineinabyss.com/mirror")
    mavenLocal()
}

dependencies {
    // MineInAbyss platform
    compileOnly(miaLibs.kotlin.stdlib)
    compileOnly(miaLibs.kotlinx.serialization.json)
    compileOnly(miaLibs.kotlinx.serialization.kaml)
    compileOnly(miaLibs.kotlinx.coroutines)
    compileOnly(miaLibs.minecraft.mccoroutine)

    // Plugin APIs
    compileOnly(miaLibs.minecraft.plugin.fawe.core)
    compileOnly(miaLibs.minecraft.plugin.fawe.bukkit) { isTransitive = false }
    compileOnly(libs.minecraft.plugin.blocklocker)

    // Shaded
    implementation(miaLibs.bundles.idofront.core)
    implementation(miaLibs.idofront.nms)
}

paper {
    name = "DeeperWorld"
    main = "com.mineinabyss.deeperworld.DeeperWorldPlugin"
    authors = listOf("Derongan", "Offz", "Boy000", "Norazan", "Scyu")
    description = "A plugin for letting you create a deeper world. Or at least fake it"

    permissions {
        register("deeperworld.*") {
            description = "Gives access to all commands"
            children = listOf("deeperworld.admin")
        }
        register("deeperworld.admin") {
            description = "Have power to use admin comands"
            default = OP
        }
        register("deeperworld.changesection") {
            description = "Allows moving between sections via automatic teleports"
            default = TRUE
        }
    }

    serverDependencies {
        register("Idofront") {
            load = BEFORE
            joinClasspath = true
        }
        register("BlockLocker") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("FastAsyncWorldEdit") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("My_Worlds") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
        register("Multiverse-Core") {
            required = false
            load = BEFORE
            joinClasspath = true
        }
    }
}
val schemas by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    runtimeClasspath += output + compileClasspath
}

dependencies {
    "schemasImplementation"("com.mineinabyss.jsonschema-kt:writer:0.2.0")
}

tasks.register<JavaExec>("generateSchemas") {
    group = "documentation"
    description = "Writes JSON schemas for configs, pass -PschemaOutput=<server-config> to write into a server config repo"
    classpath = schemas.runtimeClasspath
    mainClass = "SchemasKt"
    args(providers.gradleProperty("schemaOutput").orElse(layout.buildDirectory.dir("schemas").map { it.asFile.path }).get())
}
