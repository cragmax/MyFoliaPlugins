plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("nl.vv32.rcon:rcon:1.2.0")
    implementation("com.github.mwiede:jsch:0.2.17")
}

kotlin {
    jvmToolchain(21)
}
