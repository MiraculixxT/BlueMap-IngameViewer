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
    minecraft("com.mojang:minecraft:1.20.1")
    mappings("net.fabricmc:yarn:1.20.1+build.10:v2")
    modImplementation("net.fabricmc:fabric-loader:0.14.21")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.89.0+1.20.1")

    modApi("me.shedaniel.cloth:cloth-config-fabric:11.1.106") {
        exclude("net.fabricmc.fabric-api")
    }
    modApi("com.terraformersmc:modmenu:7.2.2")
    compileOnly("com.github.BlueMap-Minecraft:BlueMapAPI:v2.5.1")
}