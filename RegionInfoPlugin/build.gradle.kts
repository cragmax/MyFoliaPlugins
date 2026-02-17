tasks.register<Copy>("deploy") {
    dependsOn(tasks.jar)
    onlyIf { project.findProperty("isDev") == "true" }
    outputs.upToDateWhen { false }

    val rconPassword = project.property("rconPassword") as String
    val serverDir = project.property("serverDir") as String
    val pluginsDir = project.property("serverPluginsPath") as String
    val isDeployAll = project.rootProject.extensions.extraProperties.has("isDeployAll")
    val pluginName = project.name
    val pluginFileTree = project.fileTree(pluginsDir) {
        include("${pluginName}*.jar")
    }

    doFirst {
        if (!isDeployAll) {
            ServerUtils.stopServer(rconPassword)
        }

        pluginFileTree.forEach { file ->
            println("Deleting: ${file.name}")
            file.delete()
        }

        println("Deploying: $pluginName")
    }

    from(tasks.jar.map { it.outputs.files })
    into(pluginsDir)
    //duplicatesStrategy = DuplicatesStrategy.INCLUDE

    doLast {
        if (!isDeployAll) {
            ServerUtils.startServer(serverDir)
        }
    }
}

tasks.named("build") {
    finalizedBy("deploy")
}