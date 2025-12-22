package cz.jeme.bestium.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import cz.jeme.bestium.config.Config
import cz.jeme.bestium.util.bestiumComponent
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.Commands.literal
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class BestiumCommand(plugin: Plugin, commands: Commands) {
    private val command = literal("bestium")
        .requires { it.sender.hasPermission("bestium.command.bestium") }
        .then(
            literal("reload").executes(::reload)
        )
        .build()

    private fun reload(ctx: CommandContext<CommandSourceStack>): Int {
        Config.reload()
        val sender: CommandSender = ctx.source.executor ?: ctx.source.sender
        sender.sendMessage(
            bestiumComponent.append(
                Component.text(
                    "Reloaded successfully",
                    NamedTextColor.GREEN
                )
            )
        )
        return Command.SINGLE_SUCCESS
    }

    init {
        commands.register(
            plugin.pluginMeta,
            command,
            "Main command of the Bestium plugin",
            emptyList()
        )
    }
}