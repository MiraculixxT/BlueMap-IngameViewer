
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven {
        name = "JitPack"
        setUrl("https://jitpack.io")
    }
}

dependencies {
    implementation("org.yaml:snakeyaml:1.33")
    implementation("com.github.BlueMap-Minecraft:BlueMapAPI:v2.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("com.github.BlueMap-Minecraft:BlueMapAPI:v2.2.1")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}