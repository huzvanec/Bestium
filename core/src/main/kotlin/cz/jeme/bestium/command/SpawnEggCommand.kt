package cz.jeme.bestium.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import cz.jeme.bestium.util.toResourceLocation
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.Commands.argument
import io.papermc.paper.command.brigadier.Commands.literal
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.RegistryArgumentExtractor
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.item.component.TypedEntityData
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.entity.EntityType as BukkitEntityType

private val PIG_SPAWN_EGG = SpawnEggItem.byId(EntityType.PIG)!!
private const val MAX_GIVEN_SPAWN_EGGS = 64 * 100

class SpawnEggCommand(plugin: Plugin, commands: Commands) {

    private val command = literal("spawnegg")
        .requires { it.sender.hasPermission("bestium.command.spawnegg") }
        .then(
            argument("targets", ArgumentTypes.players())
                .then(
                    argument("entity", ArgumentTypes.resourceKey(RegistryKey.ENTITY_TYPE))
                        .suggests(SummonableEntitiesSuggestionProvider)
                        .executes { ctx ->
                            giveSpawnEgg(
                                ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                    .resolve(ctx.source),
                                RegistryArgumentExtractor.getTypedKey(
                                    ctx,
                                    RegistryKey.ENTITY_TYPE,
                                    "entity"
                                ),
                                PIG_SPAWN_EGG
                            )
                        }
                        .then(
                            argument("egg", SpawnEggArgumentType)
                                .executes { ctx ->
                                    giveSpawnEgg(
                                        ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                            .resolve(ctx.source),
                                        RegistryArgumentExtractor.getTypedKey(
                                            ctx,
                                            RegistryKey.ENTITY_TYPE,
                                            "entity"
                                        ),
                                        ctx.getArgument("egg", SpawnEggItem::class.java)
                                    )
                                }
                                .then(
                                    argument("count", IntegerArgumentType.integer(1, MAX_GIVEN_SPAWN_EGGS))
                                        .executes { ctx ->
                                            giveSpawnEgg(
                                                ctx.getArgument("targets", PlayerSelectorArgumentResolver::class.java)
                                                    .resolve(ctx.source),
                                                RegistryArgumentExtractor.getTypedKey(
                                                    ctx,
                                                    RegistryKey.ENTITY_TYPE,
                                                    "entity"
                                                ),
                                                ctx.getArgument("egg", SpawnEggItem::class.java),
                                                IntegerArgumentType.getInteger(ctx, "count")
                                            )
                                        }
                                )
                        )
                ))
        .build()

    private fun giveSpawnEgg(
        players: List<Player>,
        spawning: TypedKey<BukkitEntityType>,
        egg: SpawnEggItem,
        count: Int = 1
    ): Int {
        val entityType = BuiltInRegistries.ENTITY_TYPE.get(spawning.toResourceLocation())
            .get().value()

        val nmsStack = ItemStack(egg, count).apply {
            set(
                DataComponents.ENTITY_DATA,
                TypedEntityData.of(entityType, CompoundTag())
            )
        }

        val stack = CraftItemStack.asCraftMirror(nmsStack)

        val entityNameMsg = entityType.description
        val entityNameCpt = MessageComponentSerializer.message().deserialize(entityNameMsg)
        val entityNameStr = PlainTextComponentSerializer.plainText().serialize(entityNameCpt)

        @Suppress("UnstableApiUsage")
        stack.setData(
            DataComponentTypes.ITEM_NAME,
            Component.text("$entityNameStr Spawn Egg")
        )

        players.forEach { player ->
            player.inventory.addItem(stack).values.forEach {
                player.world.dropItem(player.location, it)
            }
        }

        return Command.SINGLE_SUCCESS
    }

    init {
        commands.register(
            plugin.pluginMeta,
            command,
            "Gives customized spawn eggs to players",
            emptyList<String>()
        )
    }
}