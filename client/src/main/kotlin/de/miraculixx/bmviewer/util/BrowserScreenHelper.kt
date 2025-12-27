package de.miraculixx.bmviewer.util

import com.cinemamod.mcef.MCEF
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import de.miraculixx.bmviewer.BMViewerClient
import de.miraculixx.bmviewer.screen.BrowserImpl
import de.miraculixx.bmviewer.screen.BrowserScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW


object BrowserScreenHelper {
    private const val Z_SHIFT = -1
    var currentUrl: String? = null
    val uuid = Minecraft.getInstance().player?.uuid

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
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader)
        RenderSystem.setShaderTexture(0, textureID)

        val tesselator = Tesselator.getInstance()
        val buffer = tesselator.builder

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
        buffer.vertex(offset.toDouble(), (height - offset).toDouble(), Z_SHIFT.toDouble()).uv(0.0f, 1.0f).color(255, 255, 255, 255).endVertex()
        buffer.vertex((width - offset).toDouble(), (height - offset).toDouble(), Z_SHIFT.toDouble()).uv(1.0f, 1.0f).color(255, 255, 255, 255).endVertex()
        buffer.vertex((width - offset).toDouble(), offset.toDouble(), Z_SHIFT.toDouble()).uv(1.0f, 0.0f).color(255, 255, 255, 255).endVertex()
        buffer.vertex(offset.toDouble(), offset.toDouble(), Z_SHIFT.toDouble()).uv(0.0f, 0.0f).color(255, 255, 255, 255).endVertex()

        BufferUploader.drawWithShader(buffer.end())

        RenderSystem.setShaderTexture(0, 0)
        RenderSystem.enableDepthTest()
    }

    //Navigation initialization methods
    fun initButton(message: Component, onPress: Button.OnPress, x: Int, y: Int): Button {
        return Button.builder(message, onPress)
            .bounds(x, y, 15, 15)
            .build()
    }

    //Matrix related commands
    fun mouseX(x: Double, offset: Int): Int {
        lastMouseX = x
        return ((x - offset) * Minecraft.getInstance().window.guiScale).toInt()
    }

    fun mouseY(y: Double, offset: Int): Int {
        lastMouseY = y
        return ((y - offset) * Minecraft.getInstance().window.guiScale).toInt()
    }

    fun updateMouseLocation(mouseX: Double, mouseY: Double) {
        lastMouseX = mouseX
        lastMouseY = mouseY
    }

    fun scaleX(x: Double, offset: Int): Int {
        return ((x - offset * 2) * Minecraft.getInstance().window.guiScale).toInt()
    }

    fun scaleY(y: Double, offset: Int): Int {
        return ((y - offset * 2) * Minecraft.getInstance().window.guiScale).toInt()
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

    fun initUrlBox(width: Int, height: Int): EditBox {
        val urlBox: EditBox = object : EditBox(Minecraft.getInstance().font, width / 4, height / 2, width / 2, 15, Component.literal("")) {
            override fun keyPressed(event: KeyEvent): Boolean {
                if (isFocused) {
                    if (event.key == GLFW.GLFW_KEY_ENTER) {
                        val config = BMViewerClient.getConfig()
                        if (!value.startsWith("http")) value = "http${if (config.saveProtocol) "s" else ""}://$value"
                        openScreen?.onClose()
                        config.currentUrl = value
                        Minecraft.getInstance().setScreen(BrowserScreen(Component.literal("BlueMap Viewer"), value, true))
                        return true
                    }
                }
                return super.keyPressed(event)
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
