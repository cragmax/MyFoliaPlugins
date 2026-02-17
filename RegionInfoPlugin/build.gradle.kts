tasks.register<Copy>("deployPlugin") {
    dependsOn(tasks.jar)
    from(tasks.jar.map { it.outputs.files })
    into("C:/MinecraftServers/plugins")
}

tasks.named("build") {
    finalizedBy("deployPlugin")
}