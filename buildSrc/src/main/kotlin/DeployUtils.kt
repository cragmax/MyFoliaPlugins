// ============================================================
// DeployUtils.kt
// High-level deployment orchestration.
// Coordinates jar copying and start.bat generation.
// Both deploy-convention.gradle.kts and root build.gradle.kts
// use these functions to avoid duplicating logic.
// Low-level RCON and process management lives in ServerUtils.kt.
// Takes rootDir: File instead of Project to avoid Task.project
// deprecation warnings when called from doLast.
// ============================================================

import org.gradle.api.file.FileCollection
import java.io.File

object DeployUtils {

    // ============================================================
    // Validate server and plugins directories exist before
    // attempting any file operations. Fails fast with clear messages
    // rather than letting Gradle silently create phantom folders.
    // ============================================================
    private fun validateServerDir(props: DeployProperties) {
        if (!File(props.serverDir).exists()) {
            error("[deploy] Server directory not found: ${props.serverDir} — check serverDir in gradle.properties")
        }
        if (!File(props.serverPluginsPath).exists()) {
            error("[deploy] Plugins directory not found: ${props.serverPluginsPath} — check serverPluginsPath in gradle.properties")
        }
    }

    // ============================================================
    // Deploy a single plugin jar to the server plugins folder.
    // Validates directories exist first.
    // Deletes the old version using a wildcard match so version
    // changes in the jar name don't leave stale jars.
    // e.g. MyPlugin-1.0-SNAPSHOT-feature-my-thing.jar
    // ============================================================
    fun deployJar(rootDir: File, props: DeployProperties, pluginName: String, jarFiles: FileCollection) {
        validateServerDir(props)

        val pluginsDir = File(props.serverPluginsPath)

        // Delete old version of this plugin - wildcard handles version string changes
        pluginsDir.listFiles { file ->
            file.name.startsWith(pluginName) && file.name.endsWith(".jar")
        }?.forEach { file ->
            println("[deploy] Deleting old jar: ${file.name}")
            file.delete()
        }

        // Copy new jar into plugins folder
        jarFiles.forEach { jar ->
            jar.copyTo(File(pluginsDir, jar.name), overwrite = true)
            println("[deploy] Deployed ${jar.name} -> ${props.serverPluginsPath}")
        }
    }

    // ============================================================
    // Copy start.bat from scripts/ to the server folder,
    // substituting tokens from gradle.properties so the generated
    // file has the correct jar name and memory settings baked in.
    //
    // Template tokens in scripts/start.bat:
    //   @SERVER_JAR@         -> props.serverJar
    //   @SERVER_MIN_MEMORY@  -> props.serverMinMemory
    //   @SERVER_MAX_MEMORY@  -> props.serverMaxMemory
    //
    // Never edit the generated file in the server folder directly -
    // it will be overwritten on next deploy.
    // ============================================================
    fun copyStartBat(rootDir: File, props: DeployProperties) {
        val templateFile = File(rootDir, "scripts/start.bat")
        val serverDir = File(props.serverDir)

        if (!serverDir.exists()) {
            println("[deploy] WARNING: Server directory not found: ${props.serverDir} — skipping start.bat generation")
            return
        }

        if (!templateFile.exists()) {
            println("[deploy] WARNING: scripts/start.bat template not found — skipping start.bat generation")
            return
        }

        try {
            val output = File(serverDir, "start.bat")
            output.writeText(
                templateFile.readLines().joinToString("\r\n") { line ->
                    line
                        .replace("@SERVER_JAR@", props.serverJar)
                        .replace("@SERVER_MIN_MEMORY@", props.serverMinMemory)
                        .replace("@SERVER_MAX_MEMORY@", props.serverMaxMemory)
                }
            )
            println("[deploy] Generated start.bat -> ${props.serverDir}")
        } catch (e: Exception) {
            println("[deploy] WARNING: Failed to generate start.bat - ${e.message}. Continuing deploy...")
        }
    }
}