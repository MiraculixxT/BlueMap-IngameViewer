package de.miraculixx.bmviewer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

val LOGGER: Logger = LogManager.getLogger("BMViewerServer")

class BMViewerServer : JavaPlugin() {
    private val file = File("plugins/bmviewer_server/config.json")

    companion object {
        lateinit var plugin: JavaPlugin
        lateinit var customConfig: Config
    }

    override fun onEnable() {
        plugin = this
        customConfig = ConfigLoader.loadConfig(file) ?: handleConfigError()

        server.pluginManager.registerEvents(PacketSending, this)
    }

    private fun handleConfigError(): Config {
        LOGGER.warn("Failed to load configuration!")
        return Config()
    }
}

val INSTANCE by lazy { BMViewerServer.plugin }
val CONFIG by lazy { BMViewerServer.customConfig }