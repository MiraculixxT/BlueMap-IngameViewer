package de.miraculixx.bmviewer.screen

import de.miraculixx.bmviewer.BMViewerClient
import de.miraculixx.bmviewer.util.BrowserScreenHelper
import de.miraculixx.bmviewer.util.BrowserScreenHelper.browser
import de.miraculixx.bmviewer.util.BrowserScreenHelper.isOpen
import de.miraculixx.bmviewer.util.BrowserScreenHelper.lastUrl
import de.miraculixx.bmviewer.util.sendToastMessage
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.concurrent.CompletableFuture

class BrowserScreen(title: Component?, private var initURL: String, private val sendError: Boolean = false) : Screen(title) {
    private var scale = 10

    // Only visible if no auto connect was possible
    private var urlBox: EditBox? = null

    // Only visible if map is loaded
    private var reloadButton: Button? = null

    override fun init() {
        super.init()
        isOpen = true
        val transparent = false
        BrowserScreenHelper.openScreen = this
        BrowserScreenHelper.tooltipText = null


        // Reload/Load BlueMap if needed
        if (browser == null) {
            if (checkLink(initURL)) {
                sendToastMessage(Component.literal("BlueMap Update").withStyle(ChatFormatting.GREEN), Component.literal("Loading $initURL..."))
                browser = BrowserScreenHelper.createBrowser(initURL, transparent)
                lastUrl = initURL
            } else if (sendError) {
                sendToastMessage(Component.literal("BlueMap Update").withStyle(ChatFormatting.RED), Component.literal("Target URL is not a BlueMap"))
            }
        } else if (lastUrl != initURL) {
            lastUrl = initURL
            if (checkLink(initURL)) {
                browser!!.loadURL(initURL)
            } else {
                browser!!.close()
                browser = null
                onClose()
                sendToastMessage(Component.literal("Failed Connecting!"), Component.literal("Invalid URL: $initURL"))
                return
            }
        }

        resizeWidgets()
        scale = 100 - BMViewerClient.getConfig().scale
        if (scale < 0 || scale > 50) {
            BMViewerClient.getConfig().scale = 90
            scale = 10
        }

        if (browser != null) {
            reloadButton = BrowserScreenHelper.initButton(Component.literal("⟳"), { BrowserScreenHelper.reloadButtonAction() }, 0, height - 15)
                .apply { addWidget(this) }
        } else {
            urlBox = BrowserScreenHelper.initUrlBox(width, height).apply { addWidget(this) }
        }
    }

    override fun resize(minecraft: Minecraft, i: Int, j: Int) {
        super.resize(minecraft, i, j)
        resizeWidgets()
        urlBox?.let { if (!children().contains(urlBox)) addWidget(urlBox) }
        reloadButton?.let { if (!children().contains(reloadButton)) addWidget(reloadButton) }
    }

    override fun onClose() {
        BrowserScreenHelper.openScreen = null
        isOpen = false
        super.onClose()
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (browser == null) {
            urlBox?.render(context, mouseX, mouseY, delta)
            EditBox(
                Minecraft.getInstance().font,
                width / 4, height / 2 - 12, width / 2, 15,
                Component.literal("Server did not provide a BlueMap URL!")
            ).render(context, mouseX, mouseY, delta)

            EditBox(
                Minecraft.getInstance().font,
                width / 4, height / 2 - 15, width / 2, 15,
                Component.literal("You can manually enter an URL below")
            ).render(context, mouseX, mouseY, delta)

        } else {
            BrowserScreenHelper.renderBrowser(scale, width, height, browser!!.renderer.textureID)
            reloadButton?.render(context, mouseX, mouseY, delta)
        }

        if (BrowserScreenHelper.tooltipText != null && BrowserScreenHelper.tooltipText!!.toByteArray().isNotEmpty()) {
            //setTooltip(Component.literal(BrowserScreenHelper.tooltipText))
        }
    }


    override fun mouseClicked(event: MouseButtonEvent, isClick: Boolean): Boolean {
        mouseButtonControl(event.x, event.y, event.button(), true)
        return super.mouseClicked(event, isClick)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean {
        mouseButtonControl(event.x, event.y, event.button(), false)
        return super.mouseReleased(event)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        CompletableFuture.runAsync {
            browser!!.sendMouseMove(
                BrowserScreenHelper.mouseX(mouseX, scale),
                BrowserScreenHelper.mouseY(mouseY, scale)
            )
        }
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseDragged(event: MouseButtonEvent, d: Double, e: Double): Boolean {
        BrowserScreenHelper.updateMouseLocation(event.x, event.y)
        return super.mouseDragged(event, d, e)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double, idk: Double): Boolean {
        CompletableFuture.runAsync {
            browser?.sendMouseWheel(
                BrowserScreenHelper.mouseX(mouseX, scale),
                BrowserScreenHelper.mouseY(mouseY, scale),
                delta,
                0
            )
        }
        return super.mouseScrolled(mouseX, mouseY, delta, idk)
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        setFocus()
        return super.keyPressed(event)
    }

    override fun keyReleased(event: KeyEvent): Boolean {
        CompletableFuture.runAsync { browser?.sendKeyRelease(event.key, event.scancode.toLong(), event.modifiers) }
        setFocus()
        return super.keyReleased(event)
    }

    override fun charTyped(event: CharacterEvent): Boolean {
        if (event.codepoint == 0) return false
        CompletableFuture.runAsync { browser?.sendKeyTyped(event.codepointAsString().first(), event.modifiers) }
        setFocus()
        return super.charTyped(event)
    }

    private fun setFocus() {
        if (isOverWidgets) {
            browser?.setFocus(false)
            reloadButton?.isFocused = reloadButton!!.isMouseOver(BrowserScreenHelper.lastMouseX, BrowserScreenHelper.lastMouseY)
        } else {
            reloadButton?.isFocused = reloadButton!!.isMouseOver(BrowserScreenHelper.lastMouseX, BrowserScreenHelper.lastMouseY)
            browser?.setFocus(true)
        }
    }

    private fun resizeWidgets() {
        if (width > 100 && height > 100) {
            browser?.resize(BrowserScreenHelper.scaleX(width.toDouble(), scale), BrowserScreenHelper.scaleY(height.toDouble(), scale))
        }
        urlBox?.width = width / 2
        reloadButton?.y = height - 15
    }

    private fun mouseButtonControl(mouseX: Double, mouseY: Double, button: Int, isClick: Boolean) {
        if (isClick) {
            CompletableFuture.runAsync {
                browser!!.sendMousePress(
                    BrowserScreenHelper.mouseX(mouseX, scale),
                    BrowserScreenHelper.mouseY(mouseY, scale),
                    button
                )
            }
        } else {
            CompletableFuture.runAsync {
                browser!!.sendMouseRelease(
                    BrowserScreenHelper.mouseX(mouseX, scale),
                    BrowserScreenHelper.mouseY(mouseY, scale),
                    button
                )
            }
        }
        setFocus()
    }

    private val isOverWidgets: Boolean
        get() {
            return reloadButton?.isMouseOver(BrowserScreenHelper.lastMouseX, BrowserScreenHelper.lastMouseY) ?: false
        }
    private val isButtonsFocused: Boolean
        get() {
            return reloadButton?.isFocused ?: false
        }

    private fun checkLink(link: String): Boolean {
        if (!link.startsWith("http")) return false
        return try {
            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()
            val request = HttpRequest.newBuilder(URI(link)).build()
            BMViewerClient.LOGGER.info("Connecting to $link")
            val body = client.send(request, BodyHandlers.ofString()).body()
            return true
            body.contains("Sorry but BlueMap doesn't work without JavaScript enabled", true)
        } catch (e: Exception) {
            false
        }
    }
}

