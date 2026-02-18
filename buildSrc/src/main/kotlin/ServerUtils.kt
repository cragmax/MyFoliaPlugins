import nl.vv32.rcon.Rcon
import java.io.File
import java.net.Socket

object ServerUtils {

    // All connections are local - no need to parametrise
    private const val HOST = "localhost"

    // How long to wait between each port check (ms)
    private const val POLL_INTERVAL_MS = 3000L

    // Maximum time to wait for server to stop before giving up (ms)
    private const val MAX_WAIT_MS = 60000L

    // ============================================================
    // Check if the Minecraft server is currently running
    // by attempting to open a socket connection to the MC port.
    // If the connection succeeds, the server is up.
    // If it fails, the server is down.
    // ============================================================
    private fun isServerRunning(mcPort: Int): Boolean {
        return try {
            Socket(HOST, mcPort).use { true }
        } catch (_: Exception) {
            false
        }
    }

    // ============================================================
    // Stop the server via RCON.
    // If the server is not running, logs and returns cleanly.
    // If the server is running, sends the stop command then
    // polls the MC port until it is no longer listening.
    // mcPort and rconPort come from gradle.properties via DeployProperties.
    // ============================================================
    fun stopServer(rconPassword: String, mcPort: Int, rconPort: Int) {

        if (!isServerRunning(mcPort)) {
            println("[deploy] Server is not running. Skipping stop.")
            return
        }

        try {
            println("[deploy] Connecting to RCON on port $rconPort...")
            val rcon = Rcon.open(HOST, rconPort)

            if (!rcon.authenticate(rconPassword)) {
                println("[deploy] ERROR: RCON authentication failed. Check rconPassword in gradle.properties.")
                return
            }

            println("[deploy] Sending stop command...")
            rcon.sendCommand("stop")
            rcon.close()

        } catch (e: Exception) {
            println("[deploy] WARNING: RCON failed: ${e.message}. Waiting to see if server stops anyway...")
        }

        println("[deploy] Waiting for server to stop...")
        val startTime = System.currentTimeMillis()

        while (true) {
            Thread.sleep(POLL_INTERVAL_MS)

            val elapsed = System.currentTimeMillis() - startTime

            if (!isServerRunning(mcPort)) {
                println("[deploy] Server stopped after ${elapsed / 1000}s.")
                return
            }

            if (elapsed >= MAX_WAIT_MS) {
                println("[deploy] ERROR: Server did not stop within ${MAX_WAIT_MS / 1000}s. Aborting deploy.")
                throw RuntimeException("Server shutdown timed out after ${MAX_WAIT_MS / 1000}s")
            }

            println("[deploy] Still waiting... ${elapsed / 1000}s elapsed.")
        }
    }

    // ============================================================
    // Start the server in its own terminal window.
    // cmd /c start - opens a new terminal window
    // cmd /k - keeps the window open when the server stops
    //          so errors are visible after shutdown
    // ProcessBuilder runs from serverDir so the server jar is found.
    // ============================================================
    fun startServer(serverDir: String, minMemory: String, maxMemory: String, serverJar: String) {
        println("[deploy] Starting server in $serverDir...")

        ProcessBuilder(
            "cmd", "/c", "start", "cmd", "/k",
            "java", "-Xms$minMemory", "-Xmx$maxMemory", "-jar", serverJar, "--nogui"
        )
            .directory(File(serverDir))
            .start()

        println("[deploy] Server starting in background.")
    }
}