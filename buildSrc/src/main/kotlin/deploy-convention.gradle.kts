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

    // Read isDev directly here at configuration time - cheap single
    // property read, avoids resolving all properties just for this check
    onlyIf {
        val isDev = providers.gradleProperty("isDev").orNull == "true"
        if (!isDev) {
            println("[deploy] Skipping deploy - isDev is not true in gradle.properties")
        }
        isDev
    }

    doLast {
        // Resolve all properties lazily here at execution time.
        // This means missing properties only fail when deploy actually
        // runs, not when running unrelated tasks like build.
        val props = DeployProperties.from(project)
        val jarTask = tasks.named<Jar>("jar").get()

        // Stop server before touching the plugins folder
        ServerUtils.stopServer(props.rconPassword, props.mcPort, props.rconPort)

        // Deploy this plugin's jar
        DeployUtils.deployJar(project, props, project.name, jarTask.outputs.files)

        // Generate start.bat with values from gradle.properties
        DeployUtils.copyStartBat(project, props)

        // Start server after jar is in place
        ServerUtils.startServer(props.serverDir, props.serverMinMemory, props.serverMaxMemory, props.serverJar)
    }
}