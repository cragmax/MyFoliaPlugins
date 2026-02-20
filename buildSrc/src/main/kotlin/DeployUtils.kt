import org.gradle.api.file.FileCollection
import java.io.File

object DeployUtils {

    private fun validateServerDir(props: DeployProperties) {
        if (!File(props.effectiveServerDir).exists()) {
            error("[deploy] Server directory not found: ${props.effectiveServerDir} — check serverDir in gradle.properties")
        }
        if (!File(props.effectivePluginsPath).exists()) {
            error("[deploy] Plugins directory not found: ${props.effectivePluginsPath} — check pluginsPath in gradle.properties")
        }
    }

    private fun copyFileToServer(templateFile: File, destFile: File) {
        try {
            templateFile.copyTo(destFile, overwrite = true)
            println("[deploy] Copied ${templateFile.name} -> ${destFile.parent}")
        } catch (e: Exception) {
            println("[deploy] WARNING: Failed to copy ${templateFile.name} - ${e.message}. Continuing deploy...")
        }
    }

    fun deployJar(props: DeployProperties, pluginName: String, jarFiles: FileCollection) {
        validateServerDir(props)

        val pluginsDir = File(props.effectivePluginsPath)

        // Wildcard delete handles version string changes in jar name
        // e.g. MyPlugin-1.0-SNAPSHOT-feature-my-thing.jar
        pluginsDir.listFiles { file ->
            file.name.startsWith(pluginName) && file.name.endsWith(".jar")
        }?.forEach { file ->
            println("[deploy] Deleting old jar: ${file.name}")
            file.delete()
        }

        jarFiles.forEach { jar ->
            jar.copyTo(File(pluginsDir, jar.name), overwrite = true)
            println("[deploy] Deployed ${jar.name} -> ${props.effectivePluginsPath}")
        }
    }

    fun copyStartBat(rootDir: File, props: DeployProperties) {
        val templateFile = File(rootDir, "deploy/start.bat")
        val serverDir = File(props.effectiveServerDir)

        if (!serverDir.exists()) {
            println("[deploy] WARNING: Server directory not found: ${props.effectiveServerDir} — skipping start.bat generation")
            return
        }

        if (!templateFile.exists()) {
            println("[deploy] WARNING: deploy/start.bat template not found — skipping start.bat generation")
            return
        }

        try {
            val output = File(serverDir, "start.bat")
            // Token substitution bakes memory and jar settings into the generated file
            // Never edit the generated file in the server folder - it will be overwritten on next deploy
            output.writeText(
                templateFile.readLines().joinToString("\r\n") { line ->
                    line
                        .replace("@SERVER_JAR@", props.serverJar)
                        .replace("@SERVER_MIN_MEMORY@", props.serverMinMemory)
                        .replace("@SERVER_MAX_MEMORY@", props.serverMaxMemory)
                } + "\r\n"
            )
            println("[deploy] Generated start.bat -> ${props.effectiveServerDir}")
        } catch (e: Exception) {
            println("[deploy] WARNING: Failed to generate start.bat - ${e.message}. Continuing deploy...")
        }
    }

    fun copyServerProperties(rootDir: File, props: DeployProperties) {
        val templateFile = File(rootDir, "deploy/server.properties")
        val serverDir = File(props.effectiveServerDir)

        if (!serverDir.exists()) {
            println("[deploy] WARNING: Server directory not found: ${props.effectiveServerDir} — skipping server.properties copy")
            return
        }

        if (!templateFile.exists()) {
            println("[deploy] WARNING: deploy/server.properties not found — skipping server.properties copy")
            return
        }

        copyFileToServer(templateFile, File(serverDir, "server.properties"))
    }

    fun copyOpsJson(rootDir: File, props: DeployProperties) {
        val templateFile = File(rootDir, "deploy/ops.json")
        val serverDir = File(props.effectiveServerDir)

        if (!serverDir.exists()) {
            println("[deploy] WARNING: Server directory not found: ${props.effectiveServerDir} — skipping ops.json copy")
            return
        }

        if (!templateFile.exists()) {
            println("[deploy] WARNING: deploy/ops.json not found — skipping ops.json copy")
            return
        }

        copyFileToServer(templateFile, File(serverDir, "ops.json"))
    }

    fun startServer(rootDir: File, props: DeployProperties) {
        copyServerProperties(rootDir, props)
        copyOpsJson(rootDir, props)
        copyStartBat(rootDir, props)
        if (props.serverRemote) {
            ServerUtils.launchServerProcessRemote(props)
        } else {
            ServerUtils.launchServerProcess(props.effectiveServerDir)
        }
    }
}