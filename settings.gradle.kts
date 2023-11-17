pluginManagement {
    repositories {
        repositories {
            gradlePluginPortal()
            maven("https://papermc.io/repo/repository/maven-public/")
        }
        repositories {
            gradlePluginPortal()
            maven("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
}

rootProject.name = "BMOverview"

include("server:fabric")
include("server:paper")
include("client")
include("global")