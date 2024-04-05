package de.miraculixx.bmviewer.screen

import de.miraculixx.bmviewer.BMViewerClient
import de.miraculixx.bmviewer.util.BrowserScreenHelper
import de.miraculixx.bmviewer.util.BrowserScreenHelper.browser
import de.miraculixx.bmviewer.util.BrowserScreenHelper.isOpen
import de.miraculixx.bmviewer.util.BrowserScreenHelper.lastUrl
import de.miraculixx.bmviewer.util.BrowserScreenHelper.uuid
import de.miraculixx.bmviewer.util.sendToastMessage
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.TextWidget
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.concurrent.CompletableFuture

class BrowserScreen(title: Text?, private var initURL: String, private val sendError: Boolean = false) : Screen(title) {
    private var scale = 10

    // Only visible if no auto connect was possible
    private var urlBox: TextFieldWidget? = null

    // Only visible if map is loaded
    private var reloadButton: ButtonWidget? = null

    override fun init() {
        super.init()
        isOpen = true
        val transparent = false
        BrowserScreenHelper.openScreen = this
        BrowserScreenHelper.tooltipText = null


        // Reload/Load BlueMap if needed
        if (browser == null) {
            if (checkLink(initURL)) {
                sendToastMessage(Text.literal("BlueMap Update").formatted(Formatting.GREEN), Text.literal("Loading $initURL..."))
                browser = BrowserScreenHelper.createBrowser(initURL, transparent)
                lastUrl = initURL
            } else if (sendError) {
                sendToastMessage(Text.literal("BlueMap Update").formatted(Formatting.RED), Text.literal("Target URL is not a BlueMap"))
            }
        } else if (lastUrl != initURL) {
            lastUrl = initURL
            if (checkLink(initURL)) {
                browser!!.loadURL(initURL)
            } else {
                browser!!.close()
                browser = null
                close()
                sendToastMessage(Text.of("Failed Connecting!"), Text.of("Invalid URL: $initURL"))
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
            reloadButton = BrowserScreenHelper.initButton(Text.of("⟳"), { BrowserScreenHelper.reloadButtonAction() }, 0, height - 15)
                .apply { addSelectableChild(this) }
        } else {
            urlBox = BrowserScreenHelper.initUrlBox(width, height).apply { addSelectableChild(this) }
        }
    }

    override fun resize(minecraft: MinecraftClient, i: Int, j: Int) {
        super.resize(minecraft, i, j)
        resizeWidgets()
        urlBox?.let { if (!children().contains(urlBox)) addSelectableChild(urlBox) }
        reloadButton?.let { if (!children().contains(reloadButton)) addSelectableChild(reloadButton) }
    }

    override fun close() {
        BrowserScreenHelper.openScreen = null
        isOpen = false
        super.close()
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        if (browser == null) {
            urlBox?.renderButton(context, mouseX, mouseY, delta)
            TextWidget(
                width / 4, height / 2 - 12, width / 2, 15,
                Text.literal("Server did not provide a BlueMap URL!"),
                MinecraftClient.getInstance().textRenderer
            ).render(context, mouseX, mouseY, delta)
            TextWidget(
                width / 4, height / 2 - 15, width / 2, 15,
                Text.literal("You can manually enter an URL below"),
                MinecraftClient.getInstance().textRenderer
            ).render(context, mouseX, mouseY, delta)
        } else {
            BrowserScreenHelper.renderBrowser(scale, width, height, browser!!.renderer.textureID)
            reloadButton?.render(context, mouseX, mouseY, delta)
        }

        if (BrowserScreenHelper.tooltipText != null && BrowserScreenHelper.tooltipText!!.toByteArray().isNotEmpty()) {
            setTooltip(Text.of(BrowserScreenHelper.tooltipText))
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        mouseButtonControl(mouseX, mouseY, button, true)
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        mouseButtonControl(mouseX, mouseY, button, false)
        return super.mouseReleased(mouseX, mouseY, button)
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

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        BrowserScreenHelper.updateMouseLocation(mouseX, mouseY)
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean {
        CompletableFuture.runAsync {
            browser?.sendMouseWheel(
                BrowserScreenHelper.mouseX(mouseX, scale),
                BrowserScreenHelper.mouseY(mouseY, scale),
                delta,
                0
            )
        }
        return super.mouseScrolled(mouseX, mouseY, delta)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        setFocus()

        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        CompletableFuture.runAsync { browser?.sendKeyRelease(keyCode, scanCode.toLong(), modifiers) }
        setFocus()
        return super.keyReleased(keyCode, scanCode, modifiers)
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (codePoint == 0.toChar()) return false
        CompletableFuture.runAsync { browser?.sendKeyTyped(codePoint, modifiers) }
        setFocus()
        return super.charTyped(codePoint, modifiers)
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

