package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.inject.EntityInjection;
import cz.jeme.bestium.api.inject.Injectable;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

/**
 * A context object wrapping an {@link Entity} and its properties when spawning.
 * <p>
 * This should be the only source of data for the logic
 * within {@link VariantRule#apply(Map, EntitySpawnContext)}.
 */
public final class EntitySpawnContext {
    private final Entity entity;
    private final Injectable injectable;
    private final EntityInjection<?, ?> injection;

    /**
     * Creates an entity spawn context from the given entity.
     * <p>
     * This constructor is intended for internal use only.
     *
     * @param entity the entity to create context for
     */
    @ApiStatus.Internal
    public EntitySpawnContext(final Entity entity) {
        this.entity = entity;
        final var nms = ((CraftEntity) entity).getHandle();
        if (!(nms instanceof Injectable inj)) throw new IllegalArgumentException(
                "The provided entity is not a Bestium injected entity: '" + nms.getClass().getName() + "'"
        );
        this.injectable = inj;
        injection = injectable.bestium_getInjection();
    }

    /**
     * Returns the Bukkit {@link Entity} being spawned.
     *
     * @return the spawning Bukkit entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Returns the {@link Injectable} entity being spawned.
     * <p>
     * The retured entity can be safely cast to {@link net.minecraft.world.entity.Entity}.
     * <p>
     * For most use cases, prefer using {@link #getEntity()} (Bukkit API).
     *
     * @return the spawning Minecraft injectable entity
     */
    public Injectable getInjectable() {
        return injectable;
    }

    /**
     * Returns the {@link EntityInjection} used to register this Bestium entity.
     *
     * @return the entity's Bestium injection
     */
    public EntityInjection<?, ?> getInjection() {
        return injection;
    }
}
