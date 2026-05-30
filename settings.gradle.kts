pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
        mavenLocal()
    }
}

dependencyResolutionManagement {
    val miaLibs: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
    }
    versionCatalogs{
        create("miaLibs") {
            from("com.mineinabyss:catalog:$miaLibs")
        }
    }
}

rootProject.name = "deeperworld"
