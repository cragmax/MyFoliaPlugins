import nl.vv32.rcon.Rcon
import java.io.File

object ServerUtils {
    fun stopServer(rconPassword: String) {
        try {
            val rcon = Rcon.open("localhost", 25575)
            if (rcon.authenticate(rconPassword)) {
                println("Stopping server...")
                rcon.sendCommand("stop")
                rcon.close()
                Thread.sleep(10000)
                println("Server stopped")
            } else {
                println("RCON auth failed - check password")
            }
        } catch (e: Exception) {
            println("Server not running: ${e.message}")
        }
    }

    fun startServer(serverDir: String) {
        ProcessBuilder("cmd", "/c", "start", "cmd", "/c", "start.bat")
            .directory(File(serverDir))
            .start()
        println("Server starting...")
    }
}