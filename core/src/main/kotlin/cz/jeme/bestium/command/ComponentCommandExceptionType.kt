package cz.jeme.bestium.command

import com.mojang.brigadier.ImmutableStringReader
import com.mojang.brigadier.exceptions.CommandExceptionType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import net.kyori.adventure.text.Component

class ComponentCommandExceptionType(
    val component: Component
) : CommandExceptionType {
    val message by lazy { MessageComponentSerializer.message().serialize(component) }

    fun create() = CommandSyntaxException(
        this, message
    )

    fun createWithContext(reader: ImmutableStringReader) = CommandSyntaxException(
        this, message, reader.string, reader.cursor
    )
}