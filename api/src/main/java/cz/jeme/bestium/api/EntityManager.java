package cz.jeme.bestium.api;

import cz.jeme.bestium.api.inject.EntityInjection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;


/**
 * Handles the post-initialization, lifecycle and other logic and helper methods for Bestium entities.
 */
@ApiStatus.NonExtendable
public interface EntityManager {
    /**
     * Retrieves the Bestium {@link EntityManager} instance, which handles the management and lifecycle
     * of custom Bestium entities.
     *
     * @return the {@link EntityManager} instance
     */
    static EntityManager getManager() {
        return Bestium.getEntityManager();
    }

    /**
     * Checks whether the given Bukkit {@link org.bukkit.entity.Entity} is an injected entity.
     *
     * @param entity the Bukkit {@link org.bukkit.entity.Entity} to check
     * @return {@code true} if the entity was injected by Bestium, {@code false} otherwise
     */
    default boolean isInjected(final org.bukkit.entity.Entity entity) {
        return isInjected(((CraftEntity) entity).getHandle());
    }

    /**
     * Checks whether the given Minecraft {@link Entity} is an injected entity.
     *
     * @param entity the Minecraft {@link Entity} to check
     * @return {@code true} if the entity was injected by Bestium, {@code false} otherwise
     */
    default boolean isInjected(final Entity entity) {
        return isInjected(entity.getType());
    }

    /**
     * Checks whether the given {@link EntityType} is an injected entity type.
     *
     * @param type the {@link EntityType} to check
     * @return {@code true} if the entity type was injected by Bestium, {@code false} otherwise
     */
    boolean isInjected(final EntityType<?> type);


    /**
     * Retrieves the Bestium {@link EntityInjection} of the injected Bukkit {@link org.bukkit.entity.Entity}, or {@code null} if not injected.
     *
     * @param entity the Bukkit {@link org.bukkit.entity.Entity} to retrieve the injection from
     * @return the {@link EntityInjection} used to inject the entity type, or {@code null} if the entity is not an injected entity
     */
    default @Nullable EntityInjection<?, ?> getInjection(final org.bukkit.entity.Entity entity) {
        return getInjection(((CraftEntity) entity).getHandle());
    }

    /**
     * Retrieves the Bestium {@link EntityInjection} of the injected Minecraft {@link Entity}, or {@code null} if not injected.
     *
     * @param entity the Minecraft {@link Entity} to retrieve the injection from
     * @return the {@link EntityInjection} used to inject the entity type, or {@code null} if the entity is not an injected entity
     */
    @Nullable
    EntityInjection<?, ?> getInjection(final Entity entity);

    /**
     * Retrieves the Bestium {@link EntityInjection} of the injected Bukkit {@link org.bukkit.entity.Entity}.
     * <p>
     * Throws an {@link IllegalArgumentException} if the entity is not an injected entity.
     *
     * @param entity the Bukkit {@link org.bukkit.entity.Entity} for which the injection is required
     * @return the {@link EntityInjection} used to inject the entity type
     * @throws IllegalArgumentException if the given entity is not an injected entity
     */
    default EntityInjection<?, ?> requireInjection(final org.bukkit.entity.Entity entity) {
        return requireInjection(((CraftEntity) entity).getHandle());
    }

    /**
     * Retrieves the Bestium {@link EntityInjection} of the injected Minecraft {@link Entity}.
     * <p>
     * Throws an {@link IllegalArgumentException} if the given entity is not an injected entity.
     *
     * @param entity the Minecraft {@link Entity} for which the injection is required
     * @return the {@link EntityInjection} associated with the given entity
     * @throws IllegalArgumentException if the provided entity is not an injected entity
     */
    default EntityInjection<?, ?> requireInjection(final Entity entity) {
        final var injection = getInjection(entity);
        if (injection == null) throw new IllegalArgumentException(
                "The provided entity is not an injected entity: '" + entity.getClass().getName() + "'"
        );
        return injection;
    }

    /**
     * Spawns a custom injected entity of the specified class at the given location.
     *
     * @param location            the location where the entity should spawn
     * @param entityClass         the class of the injected entity to spawn
     * @param entitySpawnReason   the Minecraft {@link EntitySpawnReason} to use when spawning
     * @param creatureSpawnReason the Bukkit {@link CreatureSpawnEvent.SpawnReason} to use when triggering events
     * @param consumer            a consumer that is called after the entity is created but before it is added to the world
     * @param <T>                 the type of the entity
     * @return the spawned entity instance, or {@code null} if spawning failed
     * @throws IllegalArgumentException if the given class is not a registered injected entity
     */
    @Nullable
    <T extends Entity> T spawn(
            final Location location,
            final Class<T> entityClass,
            final EntitySpawnReason entitySpawnReason,
            final CreatureSpawnEvent.SpawnReason creatureSpawnReason,
            final Consumer<T> consumer
    );

    /**
     * Spawns a custom injected entity with default initialization logic.
     *
     * @param location            the location where the entity should spawn
     * @param entityClass         the class of the injected entity to spawn
     * @param entitySpawnReason   the Minecraft {@link EntitySpawnReason} to use when spawning
     * @param creatureSpawnReason the Bukkit {@link CreatureSpawnEvent.SpawnReason} to use when triggering events
     * @param <T>                 the type of the entity
     * @return the spawned entity instance, or {@code null} if spawning failed
     * @throws IllegalArgumentException if the given class is not a registered injected entity
     * @see #spawn(Location, Class, EntitySpawnReason, CreatureSpawnEvent.SpawnReason, Consumer)
     */
    @Nullable
    default <T extends Entity> T spawn(
            final Location location,
            final Class<T> entityClass,
            final EntitySpawnReason entitySpawnReason,
            final CreatureSpawnEvent.SpawnReason creatureSpawnReason
    ) {
        return spawn(
                location,
                entityClass,
                entitySpawnReason,
                creatureSpawnReason,
                entity -> {
                }
        );
    }

    /**
     * Retrieves the backing type of a given injected entity type.
     *
     * @param type the {@link EntityType} to retrieve the backing type for
     * @return the backing {@link EntityType}, or {@code null} if type is not injected
     */
    @Nullable
    EntityType<?> getBackingType(final EntityType<?> type);

    /**
     * Retrieves the backing {@link EntityType} of the given Minecraft {@link Entity}.
     *
     * @param entity the Minecraft {@link Entity} to retrieve the backing type for
     * @return the backing {@link EntityType}, or {@code null} if the entity's type is not injected
     */
    @Nullable
    default EntityType<?> getBackingType(final Entity entity) {
        return getBackingType(entity.getType());
    }

    /**
     * Retrieves the backing {@link EntityType} of the given Bukkit {@link org.bukkit.entity.Entity}.
     *
     * @param entity the Bukkit {@link org.bukkit.entity.Entity} to retrieve the backing type for
     * @return the backing {@link EntityType}, or {@code null} if the entity's type is not injected
     */
    @Nullable
    default EntityType<?> getBackingType(final org.bukkit.entity.Entity entity) {
        return getBackingType(((CraftEntity) entity).getHandle());
    }
}
