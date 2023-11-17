plugins {
    `core-script`
    `fabric-script`
}

version = "1.0.0"
group = "de.miraculixx"

dependencies {
    implementation(include(project(":global"))!!)
}
