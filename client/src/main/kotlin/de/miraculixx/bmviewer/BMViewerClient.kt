package de.miraculixx.bmviewer

import com.mojang.blaze3d.platform.InputConstants
import de.bluecolored.bluemap.api.BlueMapAPI
import de.miraculixx.bmviewer.config.BrowserAutoConfig
import de.miraculixx.bmviewer.screen.BrowserScreen
import de.miraculixx.bmviewer.util.BrowserScreenHelper
import de.miraculixx.bmviewer.util.BrowserScreenHelper.browser
import de.miraculixx.bmviewer.util.BrowserScreenHelper.uuid
import de.miraculixx.bmviewer.util.sendToastMessage
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW
import java.util.logging.Logger
import kotlin.jvm.optionals.getOrNull


class BMViewerClient : ClientModInitializer {

    companion object {
        val LOGGER : Logger = Logger.getLogger("BMViewer")
        val MOD_ID = "bmviewer"
        fun getConfig(): BrowserAutoConfig = AutoConfig.getConfigHolder(BrowserAutoConfig::class.java).config
    }

    private lateinit var keyOpenMap: KeyMapping
    private val client = Minecraft.getInstance()
    private var valid = false

    override fun onInitializeClient() {
        AutoConfig.register(BrowserAutoConfig::class.java) { definition: Config?, configClass: Class<BrowserAutoConfig> ->
            GsonConfigSerializer(definition, configClass)
        }

        // Ensure MC client is initialised/available

        keyOpenMap = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                "key.bmviewer.open_map",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "hotkey"))
            )
        )

        if (FabricLoader.getInstance().getModContainer("mcef").getOrNull() != null) {
            valid = true
        }

        ClientPlayConnectionEvents.DISCONNECT.register { listener, minecraft ->
            if (!valid) return@register
            val browser = browser
            if (browser != null) {
                LOGGER.info("Closing Browser")
                browser.close(true)
                BrowserScreenHelper.browser = null
            }
        }

        ClientTickEvents.END_CLIENT_TICK.register { mc ->
            while (keyOpenMap.consumeClick()) {
                if (!valid) {
                    sendToastMessage(Component.literal("Failed Displaying Map"), Component.literal("Please install MCEF to use BlueMap Viewer"))
                    return@register
                }

                if (mc.screen is BrowserScreen) {
                    browser?.close()
                    return@register
                }

                client.player?.displayClientMessage(Component.literal("Open BlueMap!"), true)
                mc.setScreen(BrowserScreen(Component.literal("BlueMap Viewer"), getConfig().currentUrl))
                browser?.executeJavaScript("bluemap.mapViewer.controlsManager.controls.data.followingPlayer = bluemap.mapViewer.markers.markerSets.get(\"bm-players\").markers.get(\"bm-player-$uuid\");", "", 0)
            }
        }


        ClientPlayConnectionEvents.JOIN.register { listener, sender, minecraft ->
            if (minecraft.currentServer != null) {
                LOGGER.info("Integrated Server detected. Trying to connect to BlueMap...")
                if (FabricLoader.getInstance().getModContainer("bluemap").getOrNull() == null) {
                    client.player?.displayClientMessage(Component.literal("You must install BlueMap on your client to use BlueMap Viewer in Single Player").withStyle(ChatFormatting.RED), true)
                } else {
                    BlueMapAPI.onEnable {
                        getConfig().currentUrl = "http://localhost:8100"
                        sendToastMessage(Component.literal("BlueMap Connected").withStyle(ChatFormatting.GREEN), Component.literal("Successfully connected to local BlueMap"))
                    }
                }
            }
        }

    }
}
