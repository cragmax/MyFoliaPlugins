val gitBranch = providers.exec {
    commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
}.standardOutput.asText.get().trim()

subprojects {
    apply(plugin = "java")

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

    afterEvaluate {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

tasks.register("deployAll") {
    onlyIf { project.findProperty("isDev") == "true" }
    outputs.upToDateWhen { false }

    subprojects.forEach { dependsOn(it.tasks.named("jar")) }

    val rconPassword = project.property("rconPassword") as String
    val serverDir = project.property("serverDir") as String
    val pluginsDir = project.property("serverPluginsPath") as String

    doFirst {
        ServerUtils.stopServer(rconPassword)

        subprojects.forEach { sub ->
            val jarTask = sub.tasks.named("jar").get() as Jar
            fileTree(pluginsDir) {
                include("${sub.name}*.jar")
            }.forEach { file ->
                println("Deleting: ${file.name}")
                file.delete()
            }
            copy {
                from(jarTask.outputs.files)
                into(pluginsDir)
            }
            println("Deployed: ${sub.name}")
        }
    }

    doLast {
        ServerUtils.startServer(serverDir)
    }
}