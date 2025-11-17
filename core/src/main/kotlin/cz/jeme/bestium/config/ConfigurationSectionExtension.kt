package cz.jeme.bestium.config

import org.bukkit.configuration.ConfigurationSection

inline fun <reified T : Enum<T>> ConfigurationSection.getEnum(
    path: String,
    def: T? = null,
    ignoreCase: Boolean = false
): T? {
    val name = getString(path) ?: return def
    return enumValues<T>().firstOrNull {
        it.name.equals(name, ignoreCase)
    } ?: def
}