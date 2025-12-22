package cz.jeme.bestium.config

import cz.jeme.bestium.dataFolder
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

val logNormal get() = Config.logLevel >= Config.LogLevel.NORMAL
val logVerbose get() = Config.logLevel === Config.LogLevel.VERBOSE

object Config {
    private val logger = ComponentLogger.logger("BestiumConfig")
    private lateinit var config: ConfigurationSection
    private val configFile = dataFolder.resolve("config.yml").toFile()
    private val entries = mutableSetOf<ConfigEntry<*>>()

    fun reload() {
        trySaveDefaultConfig()
        config = YamlConfiguration.loadConfiguration(configFile)
        entries.forEach(ConfigEntry<*>::markDirty)
        if (logVerbose) logger.info("Reloaded configuration")
    }

    private fun trySaveDefaultConfig() {
        if (configFile.exists()) return
        javaClass.classLoader.getResourceAsStream("config.yml")?.use { input ->
            configFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not find default config.yml in plugin resources")
    }

    private fun <T> entry(initialize: () -> T): ConfigEntry<T> {
        val entry = ConfigEntry(initialize)
        entries += entry
        return entry
    }

    // CONFIG START

    enum class LogLevel { QUIET, NORMAL, VERBOSE }

    val logLevel: LogLevel by entry { config.getEnum<LogLevel>("log-level", LogLevel.NORMAL, ignoreCase = true)!! }
}
