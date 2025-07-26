package cz.jeme.bestium.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import cz.jeme.bestium.BestiumPlugin
import cz.jeme.bestium.util.toResourceLocation
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.SharedSuggestionProvider.suggestResource
import net.minecraft.commands.synchronization.SuggestionProviders
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EntityType
import java.util.concurrent.CompletableFuture

object SummonableEntitiesSuggestionProvider : SuggestionProvider<CommandSourceStack> {
    init {
        @Suppress("UNCHECKED_CAST")
        SuggestionProviders.register<SharedSuggestionProvider>(
            BestiumPlugin.createKey("summonable_entities").toResourceLocation(),
            this as SuggestionProvider<SharedSuggestionProvider>
        )
    }

    override fun getSuggestions(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val features = (context.source as SharedSuggestionProvider).enabledFeatures()

        return suggestResource(
            BuiltInRegistries.ENTITY_TYPE
                .filter { it.canSummon() }
                .filter { it.isEnabled(features) },
            builder,
            EntityType<*>::getKey,
            EntityType<*>::getDescription
        )
    }
}