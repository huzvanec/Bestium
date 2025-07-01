package cz.jeme.bestium.api.inject;

import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.CraftServer;

import java.util.function.BiFunction;

/**
 * A functional interface representing a conversion function that transforms a Minecraft entity of type {@code M}
 * into a Bukkit entity of type {@code E}, using the given {@link CraftServer} context.
 *
 * @param <M> the type of the original Minecraft entity (must extend {@link net.minecraft.world.entity.Entity})
 * @param <E> the type of the resulting Bukkit entity (must extend {@link org.bukkit.entity.Entity})
 */
@FunctionalInterface
public interface ConvertFunction<M extends Entity, E extends org.bukkit.entity.Entity> extends BiFunction<CraftServer, M, E> {
}

