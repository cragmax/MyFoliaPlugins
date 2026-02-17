tasks.register<Copy>("deployPlugin") {
    dependsOn(tasks.jar)
    onlyIf { project.property("isDev") == "true" }
    doFirst {
        delete(fileTree("${project.property("serverPluginsPath")}").matching {
            include("${project.name}*.jar")
        })
    }
    from(tasks.jar.map { it.outputs.files })
    into(project.property("serverPluginsPath") as String)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named("build") {
    finalizedBy("deployPlugin")
}