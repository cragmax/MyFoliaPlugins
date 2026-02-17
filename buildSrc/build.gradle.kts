plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("nl.vv32.rcon:rcon:1.2.0")
}

kotlin {
    jvmToolchain(21)
}