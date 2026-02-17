plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.register<Copy>("deployPlugin") {
    // Only run if jar task succeeded
    dependsOn(tasks.jar)

    from(tasks.jar)
    into("C:/MinecraftServers/plugins")

    // Only copy if the jar was built successfully
    onlyIf {
        tasks.jar.get().didWork
    }
}

// Deploy automatically after successful build
tasks.named("build") {
    finalizedBy("deployPlugin")
}