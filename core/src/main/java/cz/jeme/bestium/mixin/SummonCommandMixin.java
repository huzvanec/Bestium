package cz.jeme.bestium.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(SummonCommand.class)
abstract class SummonCommandMixin {
    // Replace the registered SUMMONABLE_ENTITIES suggestion provider with a wrapper lambda.
    // Registered providers are sent to the client by name and evaluated against the CLIENT registry.
    // When wrapped, the client asks the server to evaluate it agains its own (modified) entity type registry.
    @ModifyArg(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;suggests(Lcom/mojang/brigadier/suggestion/SuggestionProvider;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;"
            ),
            index = 0
    )
    @SuppressWarnings("FunctionalExpressionCanBeFolded")
    private static <T> SuggestionProvider<T> serverSideSuggestions(final SuggestionProvider<T> original) {
        return original::getSuggestions;
    }

    // Swap the argument type that gets synced to the client.
    // (Previous) ResourceArgument looks the value up in the client's registry while parsing.
    // (New) ResourceKeyArgument only parses "namespace:path" into a ResourceKey, so the client accepts any id.
    @ModifyArg(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/Commands;argument(Ljava/lang/String;Lcom/mojang/brigadier/arguments/ArgumentType;)Lcom/mojang/brigadier/builder/RequiredArgumentBuilder;"
            ),
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=entity"),
                    to = @At(value = "CONSTANT", args = "stringValue=pos")
            ),
            index = 1
    )
    private static ArgumentType<?> lazyEntity(final ArgumentType<?> original) {
        return ResourceKeyArgument.key(Registries.ENTITY_TYPE);
    }

    @Shadow
    @Final
    private static SimpleCommandExceptionType ERROR_FAILED;

    // Targets multiple synthetic methods created by compiling executes() blocks in summon command definition.
    // Every call to getSummonableEntityType() is replaced by serverside registry lookup,
    // because the argument now holds a ResourceKey instead of a Holder.
    @Redirect(
            method = "/lambda\\$register\\$\\d+/",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/arguments/ResourceArgument;getSummonableEntityType(Lcom/mojang/brigadier/context/CommandContext;Ljava/lang/String;)Lnet/minecraft/core/Holder$Reference;"
            )
    )
    private static Holder.Reference<EntityType<?>> lookupServerSide(
            final CommandContext<CommandSourceStack> context, final String name
    ) throws CommandSyntaxException {
        @SuppressWarnings("unchecked") final ResourceKey<EntityType<?>> key = context.getArgument(name, ResourceKey.class);

        final Holder.Reference<EntityType<?>> ref = BuiltInRegistries.ENTITY_TYPE.get(key).orElseThrow(() -> ERROR_FAILED.create());
        if (!ref.value().canSummon()) {
            throw ERROR_FAILED.create();
        }
        return ref;
    }
}