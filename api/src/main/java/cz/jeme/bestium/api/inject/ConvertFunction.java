package cz.jeme.bestium.api.inject;

import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.CraftServer;

import java.util.function.BiFunction;

@FunctionalInterface
public interface ConvertFunction<M extends Entity, E extends org.bukkit.entity.Entity> extends BiFunction<CraftServer, M, E> {
}

