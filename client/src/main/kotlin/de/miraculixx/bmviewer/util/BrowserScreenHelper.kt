package de.miraculixx.bmviewer.util

import com.cinemamod.mcef.MCEF
import com.mojang.blaze3d.systems.RenderSystem
import de.miraculixx.bmviewer.BMViewerClient
import de.miraculixx.bmviewer.screen.BrowserImpl
import de.miraculixx.bmviewer.screen.BrowserScreen
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW


object BrowserScreenHelper {
    private const val Z_SHIFT = -1
    var currentUrl: String? = null
    val uuid = MinecraftClient.getInstance().player?.uuid

    //Mouse position
    var lastMouseX = 0.0
    var lastMouseY = 0.0

    //Screen
    var openScreen: BrowserScreen? = null
    var tooltipText: String? = null

    //Data
    var lastUrl = "Not Loaded"
    var browser: BrowserImpl? = null
    var isOpen = false

    //Rendering
    fun renderBrowser(offset: Int, width: Int, height: Int, textureID: Int) {
        RenderSystem.disableDepthTest()
        RenderSystem.setShader { GameRenderer.getPositionTexColorProgram() }
        RenderSystem.setShaderTexture(0, textureID)
        val t = Tessellator.getInstance()
        val buffer = t.buffer
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
        buffer.vertex(offset.toDouble(), (height - offset).toDouble(), Z_SHIFT.toDouble()).texture(0.0f, 1.0f).color(255, 255, 255, 255).next()
        buffer.vertex((width - offset).toDouble(), (height - offset).toDouble(), Z_SHIFT.toDouble()).texture(1.0f, 1.0f).color(255, 255, 255, 255).next()
        buffer.vertex((width - offset).toDouble(), offset.toDouble(), Z_SHIFT.toDouble()).texture(1.0f, 0.0f).color(255, 255, 255, 255).next()
        buffer.vertex(offset.toDouble(), offset.toDouble(), Z_SHIFT.toDouble()).texture(0.0f, 0.0f).color(255, 255, 255, 255).next()
        t.draw()
        RenderSystem.setShaderTexture(0, 0)
        RenderSystem.enableDepthTest()
    }

    //Navigation initialization methods
    fun initButton(message: Text, onPress: PressAction, x: Int, y: Int): ButtonWidget {
        return ButtonWidget.builder(message, onPress)
            .dimensions(x, y, 15, 15)
            .build()
    }

    //Matrix related commands
    fun mouseX(x: Double, offset: Int): Int {
        lastMouseX = x
        return ((x - offset) * MinecraftClient.getInstance().window.scaleFactor).toInt()
    }

    fun mouseY(y: Double, offset: Int): Int {
        lastMouseY = y
        return ((y - offset) * MinecraftClient.getInstance().window.scaleFactor).toInt()
    }

    fun updateMouseLocation(mouseX: Double, mouseY: Double) {
        lastMouseX = mouseX
        lastMouseY = mouseY
    }

    fun scaleX(x: Double, offset: Int): Int {
        return ((x - offset * 2) * MinecraftClient.getInstance().window.scaleFactor).toInt()
    }

    fun scaleY(y: Double, offset: Int): Int {
        return ((y - offset * 2) * MinecraftClient.getInstance().window.scaleFactor).toInt()
    }

    //Browser Creation
    fun createBrowser(url: String, transparent: Boolean): BrowserImpl {
        return if (MCEF.isInitialized()) {
            val browser = BrowserImpl(MCEF.getClient(), url, transparent)
            browser.setCloseAllowed()
            browser.createImmediately()
            browser
        } else {
            throw RuntimeException("Chromium Embedded Framework was never initialized.")
        }
    }

    fun initUrlBox(width: Int, height: Int): TextFieldWidget {
        val urlBox: TextFieldWidget = object : TextFieldWidget(MinecraftClient.getInstance().textRenderer, width / 4, height / 2, width / 2, 15, Text.of("")) {
            override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
                if (isFocused) {
                    if (keyCode == GLFW.GLFW_KEY_ENTER) {
                        val config = BMViewerClient.getConfig()
                        if (!text.startsWith("http")) text = "http${if (config.saveProtocol) "s" else ""}://$text"
                        openScreen?.close()
                        config.currentUrl = text
                        MinecraftClient.getInstance().setScreen(BrowserScreen(Text.literal("BlueMap Viewer"), text, true))
                        return true
                    }
                }
                return super.keyPressed(keyCode, scanCode, modifiers)
            }
        }
        urlBox.setMaxLength(2048)
        return urlBox
    }

    fun reloadButtonAction() {
        val config = BMViewerClient.getConfig()
        var url = config.currentUrl
        if (!url.startsWith("http")) url = "http${if (config.saveProtocol) "s" else ""}://$url"
        browser?.loadURL(url)
    }
}
