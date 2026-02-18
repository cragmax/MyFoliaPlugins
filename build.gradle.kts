// ============================================================
// Root build.gradle.kts
// Shared subproject configuration and deployAll task.
// ============================================================

val gitBranch = providers.exec {
    commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
}.standardOutput.asText.get().trim().replace("/", "-")

subprojects {
    apply(plugin = "java")
    apply(plugin = "deploy-convention")

    group = "com.cragmax"
    version = "1.0-SNAPSHOT-$gitBranch"

    repositories {
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }

    dependencies {
        "compileOnly"("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.register("deployAll") {

    subprojects.forEach { dependsOn(it.tasks.named("jar")) }
    outputs.upToDateWhen { false }

    // Read isDev directly here at configuration time - cheap single
    // property read, avoids resolving all properties just for this check
    onlyIf {
        val isDev = providers.gradleProperty("isDev").orNull == "true"
        if (!isDev) {
            println("[deploy] Skipping deployAll - isDev is not true in gradle.properties")
        }
        isDev
    }

    doLast {
        // Resolve all properties lazily here at execution time.
        // This means missing properties only fail when deployAll actually
        // runs, not when running unrelated tasks like build.
        val props = DeployProperties.from(project)

        // Stop server once before touching any jars
        ServerUtils.stopServer(props.rconPassword, props.mcPort, props.rconPort)

        // Deploy all plugin jars in one pass
        subprojects.forEach { sub ->
            val jarTask = sub.tasks.named<Jar>("jar").get()
            DeployUtils.deployJar(project, props, sub.name, jarTask.outputs.files)
        }

        // Generate start.bat with values from gradle.properties
        DeployUtils.copyStartBat(project, props)

        // Start server once after all jars are in place
        ServerUtils.startServer(props.serverDir, props.serverMinMemory, props.serverMaxMemory, props.serverJar)
    }
}