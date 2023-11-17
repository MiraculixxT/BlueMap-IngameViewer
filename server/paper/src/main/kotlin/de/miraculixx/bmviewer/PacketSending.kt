package de.miraculixx.bmviewer

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object PacketSending: Listener {
    private val json = Json {
        encodeDefaults = true
    }
    var bytes = json.encodeToString(CONFIG).toByteArray()
        set(value) {
            field = json.encodeToString(value).toByteArray()
        }

    @EventHandler
    fun onJoin(it: PlayerJoinEvent) {
        it.player.sendPluginMessage(INSTANCE, ConfigLoader.channel, bytes)
    }
}