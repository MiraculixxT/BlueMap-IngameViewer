
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    maven("https://repo.bluecolored.de/releases")
}

dependencies {
    implementation("org.yaml:snakeyaml:1.33")
    implementation("de.bluecolored:bluemap-api:2.7.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.+")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile::class.java).configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}
