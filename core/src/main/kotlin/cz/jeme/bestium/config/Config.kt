package cz.jeme.bestium.config

import cz.jeme.bestium.dataFolder
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

val logNormal get() = Config.logLevel >= Config.LogLevel.NORMAL
val logVerbose get() = Config.logLevel === Config.LogLevel.VERBOSE

object Config {
    private lateinit var section: ConfigurationSection
    private val configFile by lazy { dataFolder.resolve("config.yml").toFile() }

    init {
        trySaveDefault()
        reload()
    }

    fun reload() {
        section = YamlConfiguration.loadConfiguration(configFile)
    }

    private fun trySaveDefault() {
        if (configFile.exists()) return
        configFile.outputStream().use { output ->
            javaClass.classLoader.getResourceAsStream("config.yml")!!.use { input ->
                input.copyTo(output)
            }
        }
    }

    enum class LogLevel { QUIET, NORMAL, VERBOSE }

    val logLevel: LogLevel get() = section.getEnum<LogLevel>("log-level", LogLevel.NORMAL, ignoreCase = true)!!
}
