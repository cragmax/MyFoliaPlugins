// ============================================================
// deploy-convention.gradle.kts
// Convention plugin applied to each subproject that needs
// a deploy task. Handles deploying a single plugin.
// deployAll in root build.gradle.kts orchestrates all plugins
// in a single stop/start cycle.
// ============================================================

tasks.register("deploy") {

    dependsOn(tasks.named("jar"))
    outputs.upToDateWhen { false }

    // Capture everything at configuration time
    val rootDir = rootProject.projectDir
    val pluginName = project.name
    val gradleProperties = project.providers

    onlyIf {
        val isDev = gradleProperties.gradleProperty("isDev").orNull == "true"
        if (!isDev) println("[deploy] Skipping deploy - isDev is not true in gradle.properties")
        isDev
    }

    doLast {
        val props = DeployProperties.from(gradleProperties)
        val jarTask = tasks.named<Jar>("jar").get()

        ServerUtils.stopServer(props.rconPassword, props.mcPort, props.rconPort)
        DeployUtils.deployJar(props, pluginName, jarTask.outputs.files)
        DeployUtils.copyStartBat(rootDir, props)
        ServerUtils.startServer(props.serverDir, props.serverMinMemory, props.serverMaxMemory, props.serverJar)
    }
}