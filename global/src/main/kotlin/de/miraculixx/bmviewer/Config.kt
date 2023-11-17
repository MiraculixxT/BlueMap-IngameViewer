package de.miraculixx.bmviewer

import kotlinx.serialization.Serializable

@Serializable
data class Config(val url: String = "https://your.domain")
