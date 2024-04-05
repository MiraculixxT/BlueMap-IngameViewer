package de.miraculixx.bmviewer

import de.bluecolored.bluemap.api.BlueMapAPI
import de.miraculixx.bmviewer.config.BrowserAutoConfig
import de.miraculixx.bmviewer.screen.BrowserScreen
import de.miraculixx.bmviewer.util.BrowserScreenHelper
import de.miraculixx.bmviewer.util.BrowserScreenHelper.browser
import de.miraculixx.bmviewer.util.BrowserScreenHelper.uuid
import de.miraculixx.bmviewer.util.sendToastMessage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.util.logging.Logger
import kotlin.jvm.optionals.getOrNull


class BMViewerClient : ClientModInitializer {

    private lateinit var keyOpenMap: KeyBinding
    private val minecraft = MinecraftClient.getInstance()
    private var valid = false

    override fun onInitializeClient() {
        AutoConfig.register(BrowserAutoConfig::class.java) { definition: Config?, configClass: Class<BrowserAutoConfig> ->
            GsonConfigSerializer(definition, configClass)
        }

        keyOpenMap = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.bmviewer.open_map",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.bmviewer"
            )
        )

        if (FabricLoader.getInstance().getModContainer("mcef").getOrNull() != null) {
            valid = true
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _: ClientPlayNetworkHandler, _: MinecraftClient ->
            if (!valid) return@register
            val browser = browser
            if (browser != null) {
                LOGGER.info("Closing Browser")
                browser.close(true)
                BrowserScreenHelper.browser = null
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            while (keyOpenMap.wasPressed()) {
                if (!valid) {
                    sendToastMessage(Text.literal("Faild Displaying Map"), Text.literal("Please install MCEF to use BlueMap Viewer"))
                    return@EndTick
                }

                if (minecraft.currentScreen is BrowserScreen) {
                    browser?.close()
                    return@EndTick
                }

                client.player?.sendMessage(Text.literal("Open BlueMap!"), true)
                minecraft.setScreen(BrowserScreen(Text.literal("BlueMap Viewer"), getConfig().currentUrl))
                browser?.executeJavaScript("bluemap.mapViewer.controlsManager.controls.data.followingPlayer = bluemap.mapViewer.markers.markerSets.get(\"bm-players\").markers.get(\"bm-player-$uuid\");", "", 0)
            }
        })


        ClientPlayConnectionEvents.JOIN.register { _: ClientPlayNetworkHandler, _: PacketSender, client: MinecraftClient ->
            if (client.server != null) {
                LOGGER.info("Integrated Server detected. Trying to connect to BlueMap...")
                if (FabricLoader.getInstance().getModContainer("bluemap").getOrNull() == null) {
                    client.player?.sendMessage(Text.literal("You must install BlueMap on your client to use BlueMap Viewer in Single Player").formatted(Formatting.RED))
                } else {
                    BlueMapAPI.onEnable {
                        getConfig().currentUrl = "http://localhost:8100"
                        sendToastMessage(Text.literal("BlueMap Connected").formatted(Formatting.GREEN), Text.literal("Successfully connected to local BlueMap"))
                    }
                }
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(
            Identifier.of(
                ConfigLoader.namespace,
                ConfigLoader.channel
            )
        ) { client: MinecraftClient, network: ClientPlayNetworkHandler, packet: PacketByteBuf, sender: PacketSender ->
            var string = packet.writtenBytes.decodeToString()
            val startIndex = string.indexOf('{')
            if (startIndex != -1) string = string.substring(startIndex)
            LOGGER.info("Packet: $string")
            val config = try {
                Json.decodeFromString<de.miraculixx.bmviewer.Config>(string)
            } catch (_: Exception) {
                LOGGER.warning("Received invalid packet from server! Did your versions match?")
                return@registerGlobalReceiver
            }
            LOGGER.info(" -> $config")
            getConfig().currentUrl = config.url
            sendToastMessage(Text.literal("BlueMap Connected").formatted(Formatting.GREEN), Text.literal("Successfully connected to servers BlueMap"))
        }
    }

    companion object {
        fun getConfig(): BrowserAutoConfig = AutoConfig.getConfigHolder(BrowserAutoConfig::class.java).config
        val LOGGER: Logger = Logger.getLogger("BMViewer")
    }
}
