package cz.jeme.bestium.command

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.Commands.argument
import io.papermc.paper.command.brigadier.Commands.literal
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.RegistryArgumentExtractor
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.text.Component
import net.minecraft.commands.arguments.CompoundTagArgument
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.level.Level
import org.bukkit.Location
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.util.CraftLocation
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.plugin.Plugin
import kotlin.jvm.optionals.getOrNull
import org.bukkit.entity.EntityType as BukkitEntityType

@Suppress("UnstableApiUsage")
class SummonCommand(plugin: Plugin, commands: Commands) {
    @Suppress("UNCHECKED_CAST")
    private val command = literal("summon")
        .requires { it.sender.isOp }
        .then(
            argument("entity", ArgumentTypes.resourceKey(RegistryKey.ENTITY_TYPE))
                .suggests(SummonableEntitiesSuggestionProvider)
                .executes { ctx ->
                    val source = ctx.source
                    spawnEntity(
                        source,
                        RegistryArgumentExtractor.getTypedKey(
                            ctx,
                            RegistryKey.ENTITY_TYPE,
                            "entity"
                        ),
                        source.location,
                        CompoundTag()
                    )
                }
                .then(
                    argument(
                        "pos",
                        ArgumentTypes.finePosition()
                    )
                        .executes { ctx ->
                            val source = ctx.source

                            spawnEntity(
                                source,
                                RegistryArgumentExtractor.getTypedKey(
                                    ctx,
                                    RegistryKey.ENTITY_TYPE,
                                    "entity"
                                ),
                                ctx.getArgument("pos", FinePositionResolver::class.java)
                                    .resolve(source)
                                    .toLocation(source.location.world),
                                CompoundTag()
                            )
                        }
                        .then(
                            argument(
                                "nbt",
                                CompoundTagArgument.compoundTag()
                            )
                                .executes { ctx ->
                                    val source = ctx.source

                                    spawnEntity(
                                        source,
                                        RegistryArgumentExtractor.getTypedKey(
                                            ctx,
                                            RegistryKey.ENTITY_TYPE,
                                            "entity"
                                        ),
                                        ctx.getArgument("pos", FinePositionResolver::class.java)
                                            .resolve(source)
                                            .toLocation(source.location.world),
                                        ctx.getArgument("nbt", CompoundTag::class.java)
                                    )
                                }
                        )
                )
        )
        .build()

    private fun spawnEntity(
        source: CommandSourceStack,
        typeKey: TypedKey<BukkitEntityType>,
        pos: Location,
        tag: CompoundTag
    ): Int {
        val randomizeProperties = tag.isEmpty

        val world = pos.world
        val level = (world as CraftWorld).handle

        if (!Level.isInSpawnableBounds(CraftLocation.toBlockPosition(pos))) throw ComponentCommandExceptionType(
            Component.translatable("commands.summon.invalidPosition")
        ).create()

        val keyStr = typeKey.asString()

        if (EntityType.byString(keyStr).isEmpty) throw ComponentCommandExceptionType(
            Component.translatable("argument.entity.options.type.invalid", Component.text(keyStr))
        ).create()

        EntityType.loadEntityRecursive(
            tag.copy().apply {
                // prevent overwriting the Minecraft id
                putString("id", keyStr)
                // prevent overwriting the Bestium id
                getCompound("BukkitValues").getOrNull()?.remove("bestium:id")
            },
            level,
            EntitySpawnReason.COMMAND
        ) { entity ->
            entity.snapTo(pos.x, pos.y, pos.z, entity.yRot, entity.xRot)
            entity.spawnReason = CreatureSpawnEvent.SpawnReason.COMMAND
            entity
        }?.let { entity ->
            if (randomizeProperties && entity is Mob) {
                entity.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(entity.blockPosition()),
                    EntitySpawnReason.COMMAND,
                    null
                )
            }
            val uuidValid = level.tryAddFreshEntityWithPassengers(
                entity, CreatureSpawnEvent.SpawnReason.COMMAND
            )
            if (!uuidValid) throw ComponentCommandExceptionType(
                Component.translatable("commands.summon.failed.uuid")
            ).create()

            (source as net.minecraft.commands.CommandSourceStack).sendSuccess(
                {
                    net.minecraft.network.chat.Component.translatable(
                        "commands.summon.success",
                        entity.displayName
                    )
                }, true
            )
        } ?: throw ComponentCommandExceptionType(
            Component.translatable("commands.summon.failed")
        ).create()

        return Command.SINGLE_SUCCESS
    }

    init {
        commands.register(
            plugin.pluginMeta,
            command,
            "A replacement for /minecraft:summon that takes injected entities into account",
            emptyList<String>()
        )
    }
}