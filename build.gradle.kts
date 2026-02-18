// ============================================================
// Root build.gradle.kts
// Shared subproject configuration and deployAll task.
// ============================================================

val gitBranch = providers.exec {
    commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
}.standardOutput.asText.get().trim().replace("/", "-")

val branchSuffix = if (gitBranch == "master" || gitBranch == "main") "" else "-$gitBranch"

subprojects {
    apply(plugin = "java")
    apply(plugin = "deploy-convention")

    group = "com.cragmax"
    version = "1.0-SNAPSHOT$branchSuffix"

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

    // Capture at configuration time to avoid Task.project deprecation
    val subprojectList = subprojects.toList()
    val rootDir = rootProject.projectDir
    val gradleProviders = providers

    onlyIf {
        val isDev = gradleProviders.gradleProperty("isDev").orNull == "true"
        if (!isDev) println("[deploy] Skipping deployAll - isDev is not true in gradle.properties")
        isDev
    }

    doLast {
        // gradleProviders captured at configuration time - no Task.project access at execution time
        val props = DeployProperties.from(gradleProviders)

        ServerUtils.stopServer(props.rconPassword, props.mcPort, props.rconPort)

        subprojectList.forEach { sub ->
            val jarTask = sub.tasks.named<Jar>("jar").get()
            DeployUtils.deployJar(rootDir, props, sub.name, jarTask.outputs.files)
        }

        DeployUtils.copyStartBat(rootDir, props)
        ServerUtils.startServer(props.serverDir, props.serverMinMemory, props.serverMaxMemory, props.serverJar)
    }
}