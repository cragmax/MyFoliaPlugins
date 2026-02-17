tasks.register<Copy>("deployPlugin") {
    dependsOn(tasks.jar)
    onlyIf { project.property("isDev") == "true" }
    outputs.upToDateWhen { false }
    doFirst {
        val pluginsDir = project.property("serverPluginsPath") as String
        fileTree(pluginsDir) {
            include("${project.name}*.jar")
        }.forEach { file ->
            println("Deleting: ${file.name}")
            file.delete()
        }
    }
    from(tasks.jar.map { it.outputs.files })
    into(project.property("serverPluginsPath") as String)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named("build") {
    finalizedBy("deployPlugin")
}