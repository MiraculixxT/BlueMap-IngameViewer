
plugins {
    kotlin("jvm")
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

repositories {
    mavenCentral()
    maven {
        name = "JitPack"
        setUrl("https://jitpack.io")
    }
}

dependencies {
    paperDevBundle("1.20.1-R0.1-SNAPSHOT")

}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}
