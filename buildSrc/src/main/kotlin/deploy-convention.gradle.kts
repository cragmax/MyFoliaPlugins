tasks.register("deploy") {

    dependsOn(tasks.named("jar"))
    outputs.upToDateWhen { false }

    // Captured at configuration time to avoid Task.project deprecation warnings
    val rootDir = rootProject.projectDir
    val pluginName = project.name
    val gradleProviders = providers

    onlyIf {
        val isDev = gradleProviders.gradleProperty("isDev").orNull == "true"
        if (!isDev) println("[deploy] Skipping deploy - isDev is not true in gradle.properties")
        isDev
    }

    doLast {
        val props = DeployProperties.from(gradleProviders)
        val jarTask = tasks.named<Jar>("jar").get()

        ServerUtils.stopServer(props)
        DeployUtils.deployJar(props, pluginName, jarTask.outputs.files)
        DeployUtils.startServer(rootDir, props)
    }
}