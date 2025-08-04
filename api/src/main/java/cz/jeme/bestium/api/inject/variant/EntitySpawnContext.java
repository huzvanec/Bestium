package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.Bestium;
import cz.jeme.bestium.api.inject.EntityInjection;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A context object wrapping an {@link Entity} and its properties when spawning.
 * <p>
 * This should be the only source of data for the logic
 * within {@link VariantRule#apply(Map, EntitySpawnContext)}.
 */
public final class EntitySpawnContext {
    private final Entity entity;
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
        injection = Bestium.getEntityManager().requireInjection(entity);
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
     * Returns the {@link EntityInjection} used to register this Bestium entity.
     *
     * @return the entity's Bestium injection
     */
    public EntityInjection<?, ?> getInjection() {
        return injection;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (!(o instanceof final EntitySpawnContext that)) return false;

        return entity.equals(that.entity);
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }
}
