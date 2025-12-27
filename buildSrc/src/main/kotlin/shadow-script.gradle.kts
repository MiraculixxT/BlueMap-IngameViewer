
plugins {
    kotlin("jvm")
    id("com.gradleup.shadow")
}

tasks {
    shadowJar {
        dependencies {
            include {
                it.moduleGroup == "de.miraculixx"
            }
        }
    }
}