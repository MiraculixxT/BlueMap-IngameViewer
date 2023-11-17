package de.miraculixx.bmviewer.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.toast.SystemToast
import net.minecraft.text.Text

fun sendToastMessage(title: Text, description: Text) {
    MinecraftClient.getInstance().toastManager.add(SystemToast(SystemToast.Type.TUTORIAL_HINT, title, description))
}