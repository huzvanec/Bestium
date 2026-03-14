package cz.jeme.bestium.config

import cz.jeme.bestium.dataFolder
import cz.jeme.bestium.util.CachedFirst
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

val logNormal get() = Config.logLevel >= Config.LogLevel.NORMAL
val logVerbose get() = Config.logLevel === Config.LogLevel.VERBOSE

object Config {
    private val logger = ComponentLogger.logger("BestiumConfig")
    private lateinit var config: ConfigurationSection
    private val configFile = dataFolder.resolve("config.yml").toFile()
    private val entries = mutableSetOf<CachedFirst.Value<*>>()

    fun reload() {
        trySaveDefaultConfig()
        config = YamlConfiguration.loadConfiguration(configFile)
        entries.forEach { it.reset() }
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

    private fun <T> entry(init: () -> T): CachedFirst<T> {
        val entry = CachedFirst.Value(init)
        entries += entry
        return entry
    }

    // CONFIG START

    enum class LogLevel { QUIET, NORMAL, VERBOSE }

    val logLevel: LogLevel by entry { config.getEnum<LogLevel>("log-level", LogLevel.NORMAL, ignoreCase = true)!! }
}
