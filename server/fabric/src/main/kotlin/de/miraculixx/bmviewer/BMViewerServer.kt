package de.miraculixx.bmviewer

import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.command.CommandSource
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.ServerCommandSource
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
    private var config = ConfigLoader.loadConfig(file) ?: handleConfigError()

    override fun onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register { server: MinecraftServer ->
            LOGGER.info(server.serverIp)
            println(server.serverIp)
        }

        val identifier = Identifier.of(ConfigLoader.namespace, ConfigLoader.channel)
        val byteBuffer = json.encodeToString(config).toByteArray()
        ServerPlayConnectionEvents.JOIN.register { network: ServerPlayNetworkHandler, sender: PacketSender, server: MinecraftServer ->
            sender.sendPacket(identifier, PacketByteBufs.create().writeByteArray(byteBuffer))
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                literal<ServerCommandSource>("bmviewer-reload")
                    .executes {
                        config = ConfigLoader.loadConfig(file) ?: handleConfigError()
                        1
                    }
            )
        }
    }

    private fun handleConfigError(): Config {
        LOGGER.warn("Failed to load configuration!")
        return Config()
    }
}