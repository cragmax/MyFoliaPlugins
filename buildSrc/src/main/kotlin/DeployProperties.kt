import org.gradle.api.provider.ProviderFactory

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
        // --------------------------------------------------------
        // Takes ProviderFactory instead of Project to avoid
        // Task.project deprecation warnings when called from doLast.
        // Capture project.providers at configuration time and pass
        // it here at execution time.
        // --------------------------------------------------------
        fun from(providers: ProviderFactory): DeployProperties {
            fun require(name: String): String =
                providers.gradleProperty(name).orNull
                    ?: error("[deploy] Missing required property '$name' in gradle.properties")

            // Resolved separately as it is optional - defaults to false if missing
            val isDev = providers.gradleProperty("isDev").orNull == "true"

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