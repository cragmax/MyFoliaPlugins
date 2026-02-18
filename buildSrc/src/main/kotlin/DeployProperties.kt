import org.gradle.api.Project

data class DeployProperties(
    val rconPassword: String,
    val serverDir: String,
    val serverPluginsPath: String,
    val serverMinMemory: String,
    val serverMaxMemory: String,
    val serverJar: String,
    val mcPort: Int,
    val rconPort: Int,
    val isDev: Boolean
) {
    companion object {
        fun from(project: Project): DeployProperties {
            fun require(name: String): String =
                project.providers.gradleProperty(name).orNull
                    ?: error("[deploy] Missing required property '$name' in gradle.properties")

            // Resolved separately as it is optional - defaults to false if missing
            val isDev = project.providers.gradleProperty("isDev").orNull == "true"

            return DeployProperties(
                rconPassword = require("rconPassword"),
                serverDir = require("serverDir"),
                serverPluginsPath = require("serverPluginsPath"),
                serverMinMemory = require("serverMinMemory"),
                serverMaxMemory = require("serverMaxMemory"),
                serverJar = require("serverJar"),
                mcPort = require("mcPort").toInt(),
                rconPort = require("rconPort").toInt(),
                isDev = isDev
            )
        }
    }
}