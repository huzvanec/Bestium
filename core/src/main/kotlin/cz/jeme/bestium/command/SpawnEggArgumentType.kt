package cz.jeme.bestium.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.SpawnEggItem
import java.util.concurrent.CompletableFuture

object SpawnEggArgumentType : CustomArgumentType<SpawnEggItem, String> {
    override fun parse(reader: StringReader): SpawnEggItem {
        val start = reader.cursor
        while (reader.canRead() && reader.peek() != ' ') reader.read()
        val string = reader.string.substring(start, reader.cursor)

        val unknownEntity by lazy {
            ComponentCommandExceptionType(
                Component.translatable("argument.entity.options.type.invalid", Component.text(string))
            ).createWithContext(reader)
        }

        if (!Key.parseable(string)) throw unknownEntity
        val id = Identifier.parse(string)
        val entityType = BuiltInRegistries.ENTITY_TYPE.get(id).get().value()
        return SpawnEggItem.byId(entityType) ?: throw unknownEntity
    }

    override fun getNativeType(): ArgumentType<String> = StringArgumentType.greedyString()

    private val suggestions = BuiltInRegistries.ENTITY_TYPE
        .filter { SpawnEggItem.byId(it) != null }
        .map { BuiltInRegistries.ENTITY_TYPE.getKey(it).toString() }


    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        suggestions.forEach { builder.suggest(it) }

        return builder.buildFuture()
    }
}