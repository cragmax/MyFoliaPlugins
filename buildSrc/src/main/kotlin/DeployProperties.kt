import org.gradle.api.provider.ProviderFactory

data class DeployProperties(
    val rconPassword: String,
    val remoteServerDir: String,
    val remotePluginsPath: String,
    // Local path on the server machine itself - used for SSH commands, not file copying
    val remoteServerLocalDir: String,
    val localServerDir: String,
    val localPluginsPath: String,
    val serverMinMemory: String,
    val serverMaxMemory: String,
    val serverJar: String,
    val remoteMcHost: String,
    val mcPort: Int,
    val remoteRconHost: String,
    val rconPort: Int,
    val serverRemote: Boolean,
    // Only required when serverRemote=true
    val serverSshUser: String?,
    val serverSshKeyPath: String?,
) {
    val effectiveServerDir: String get() = if (serverRemote) remoteServerDir else localServerDir
    val effectivePluginsPath: String get() = if (serverRemote) remotePluginsPath else localPluginsPath

    // Defaults to localhost when serverRemote=false since server runs on the dev machine
    val effectiveMcHost: String get() = if (serverRemote) remoteMcHost else "localhost"
    val effectiveRconHost: String get() = if (serverRemote) remoteRconHost else "localhost"

    companion object {
        fun from(providers: ProviderFactory): DeployProperties {
            fun require(name: String): String =
                providers.gradleProperty(name).orNull
                    ?: error("[deploy] Missing required property '$name' in gradle.properties")

            val serverRemote = providers.gradleProperty("serverRemote").orNull == "true"

            val serverSshUser = providers.gradleProperty("serverSshUser").orNull
            val serverSshKeyPath = providers.gradleProperty("serverSshKeyPath").orNull

            if (serverRemote) {
                checkNotNull(serverSshUser) { "[deploy] Missing required property 'serverSshUser' in gradle.properties when serverRemote=true" }
                checkNotNull(serverSshKeyPath) { "[deploy] Missing required property 'serverSshKeyPath' in gradle.properties when serverRemote=true" }
            }

            return DeployProperties(
                rconPassword = require("rconPassword"),
                remoteServerDir = require("remoteServerDir"),
                remotePluginsPath = require("remotePluginsPath"),
                remoteServerLocalDir = require("remoteServerLocalDir"),
                localServerDir = require("localServerDir"),
                localPluginsPath = require("localPluginsPath"),
                serverMinMemory = require("serverMinMemory"),
                serverMaxMemory = require("serverMaxMemory"),
                serverJar = require("serverJar"),
                remoteMcHost = require("remoteMcHost"),
                mcPort = require("mcPort").toInt(),
                remoteRconHost = require("remoteRconHost"),
                rconPort = require("rconPort").toInt(),
                serverRemote = serverRemote,
                serverSshUser = serverSshUser,
                serverSshKeyPath = serverSshKeyPath,
            )
        }
    }
}