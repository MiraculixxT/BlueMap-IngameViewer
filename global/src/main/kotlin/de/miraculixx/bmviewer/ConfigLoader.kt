package de.miraculixx.bmviewer

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object ConfigLoader {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }
    val channel = "config"

    fun loadConfig(file: File): Config? {
        return if (!file.exists()) {
            file.parentFile.mkdir()
            val newConfig = Config()
            file.writeText(json.encodeToString(newConfig))
            newConfig
        } else {
            val content = file.readText()
            loadConfig(content)
        }
    }

    fun loadConfig(string: String): Config? {
        return try {
            json.decodeFromString(string)
        } catch (_: Exception) {
            null
        }
    }
}