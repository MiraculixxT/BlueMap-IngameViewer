plugins {
    id("fabric-loom")
}

repositories {
    mavenCentral()
    maven("https://mcef-download.cinemamod.com/repositories/releases")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://jitpack.io")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${properties["kotlin_version"]}")

    modApi("me.shedaniel.cloth:cloth-config-fabric:${properties["cloth_config_version"]}") {
        exclude("net.fabricmc.fabric-api")
    }
    modApi("com.terraformersmc:modmenu:${properties["modmenu_version"]}") {
        exclude("net.fabricmc.fabric-api")
    }
}