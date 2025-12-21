package cz.jeme.bestium.util

import net.kyori.adventure.key.Key
import net.minecraft.resources.Identifier
import org.bukkit.NamespacedKey

fun Key.toNamespacedKey(): NamespacedKey = this as? NamespacedKey ?: NamespacedKey.fromString(asString())!!

fun Key.toIdentifier(): Identifier = Identifier.fromNamespaceAndPath(namespace(), value())
