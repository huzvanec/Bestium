package cz.jeme.bestium.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

val bestiumComponent = Component.text("[", NamedTextColor.DARK_GRAY)
    .append(Component.text("Bestium", NamedTextColor.GOLD))
    .append(Component.text("] ", NamedTextColor.DARK_GRAY))
