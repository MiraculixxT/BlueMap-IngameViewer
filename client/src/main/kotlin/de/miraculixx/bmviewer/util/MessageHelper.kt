package de.miraculixx.bmviewer.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.network.chat.Component

fun sendToastMessage(title: Component, description: Component) {
    Minecraft.getInstance().toastManager.addToast(SystemToast( SystemToast.SystemToastId.PERIODIC_NOTIFICATION, title, description))
}