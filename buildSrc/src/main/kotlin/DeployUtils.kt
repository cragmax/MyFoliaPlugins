// ============================================================
// DeployUtils.kt
// High-level deployment orchestration.
// Coordinates jar copying and start.bat generation.
// Both deploy-convention.gradle.kts and root build.gradle.kts
// use these functions to avoid duplicating logic.
// Low-level RCON and process management lives in ServerUtils.kt.
// ============================================================

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

object DeployUtils {

    // ============================================================
    // Deploy a single plugin jar to the server plugins folder.
    // Deletes the old version first using a wildcard match so
    // version changes in the jar name don't leave stale jars.
    // e.g. RegionInfoPlugin-1.0-SNAPSHOT-master.jar
    // ============================================================
    fun deployJar(project: Project, props: DeployProperties, pluginName: String, jarFiles: FileCollection) {

        // Delete old version of this plugin - wildcard handles version string changes
        project.fileTree(props.serverPluginsPath) {
            include("${pluginName}*.jar")
        }.forEach { file ->
            println("[deploy] Deleting old jar: ${file.name}")
            file.delete()
        }

        // Copy new jar into plugins folder
        project.copy {
            from(jarFiles)
            into(props.serverPluginsPath)
        }
        jarFiles.forEach { jar ->
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
    fun copyStartBat(project: Project, props: DeployProperties) {
        val templateFile = project.rootProject.file("scripts/start.bat")

        // Warn and skip if template is missing - deploy continues without it
        if (!templateFile.exists()) {
            println("[deploy] WARNING: scripts/start.bat template not found - skipping start.bat generation")
            return
        }

        try {
            project.copy {
                from(templateFile)
                into(props.serverDir)
                filter { line ->
                    line
                        .replace("@SERVER_JAR@", props.serverJar)
                        .replace("@SERVER_MIN_MEMORY@", props.serverMinMemory)
                        .replace("@SERVER_MAX_MEMORY@", props.serverMaxMemory)
                }
            }
            println("[deploy] Generated start.bat -> ${props.serverDir}")
        } catch (e: Exception) {
            println("[deploy] WARNING: Failed to generate start.bat - ${e.message}. Continuing deploy...")
        }
    }
}