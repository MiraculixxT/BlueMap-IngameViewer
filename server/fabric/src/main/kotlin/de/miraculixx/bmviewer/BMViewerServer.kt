package de.miraculixx.bmviewer

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

val LOGGER: Logger = LogManager.getLogger("BMViewerServer")

class BMViewerServer : DedicatedServerModInitializer {
    private val json = Json {
        encodeDefaults = true
    }
    private val file = File("config/bmviewer_server.json")
    private val config = ConfigLoader.loadConfig(file) ?: handleConfigError()

    override fun onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register { server: MinecraftServer ->
            LOGGER.info(server.serverIp)
        }

        val identifier = Identifier.of("bmviewer", ConfigLoader.channel)
        var byteBuffer = json.encodeToString(config).toByteArray()
        ServerPlayConnectionEvents.JOIN.register { network: ServerPlayNetworkHandler, sender: PacketSender, server: MinecraftServer ->
            sender.sendPacket(identifier, PacketByteBufs.create().writeByteArray(byteBuffer))
        }

        // Command for reloading
    }

    private fun handleConfigError(): Config {
        LOGGER.warn("Failed to load configuration!")
        return Config()
    }
}