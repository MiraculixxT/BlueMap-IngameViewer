plugins {
    `core-script`
    `fabric-script`
}

version = "1.0.0"
group = "de.miraculixx"

dependencies {
    modImplementation("com.cinemamod:mcef:2.1.1-1.20.1")
    modRuntimeOnly("com.cinemamod:mcef-fabric:2.1.1-1.20.1")
    implementation(include(project(":global"))!!)
}