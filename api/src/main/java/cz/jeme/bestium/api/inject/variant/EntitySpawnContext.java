package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.Bestium;
import cz.jeme.bestium.api.inject.EntityInjection;
import org.bukkit.craftbukkit.entity.CraftEntity;
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
     * @throws IllegalArgumentException if the given entity is not an injected entity
     */
    @ApiStatus.Internal
    public EntitySpawnContext(final Entity entity) {
        this(entity, Bestium.getEntityManager().requireInjection(entity));
    }

    /**
     * Creates an entity spawn context from the given entity and its injection.
     * <p>
     * This constructor is intended for internal use only.
     *
     * @param entity    the entity to create context for
     * @param injection the entity's injection
     * @throws IllegalArgumentException if the injection does not belong to the given entity
     */
    @ApiStatus.Internal
    public EntitySpawnContext(final Entity entity, final EntityInjection<?, ?> injection) {
        this.entity = entity;
        this.injection = injection;

        final var injEntityClass = injection.getEntityClass();
        final var entityClass = ((CraftEntity) entity).getHandle().getClass();

        if (injEntityClass != entityClass) throw new IllegalArgumentException(
                "Illegal spawn context arguments: Injection for type '" +
                injEntityClass.getName() + "', but entity is of type '" +
                entityClass.getName() + "'"
        );
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
