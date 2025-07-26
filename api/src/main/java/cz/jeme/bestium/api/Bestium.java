package cz.jeme.bestium.api;

import cz.jeme.bestium.api.inject.EntityInjector;
import cz.jeme.bestium.api.inject.Injectable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The core system for managing custom Minecraft entities.
 */
@NullMarked
public interface Bestium {
    /**
     * Holds all the internal singleton instances.
     * <p>
     * This class is not intended for public use.
     * To access the stored instances use static methods in this interface.
     */
    @ApiStatus.Internal
    final class InstanceHolder {
        private InstanceHolder() {
            throw new AssertionError();
        }

        /**
         * Returns the instance object by class name.
         *
         * @param className full name of the instance class
         * @return the instance object
         * @throws IllegalStateException if the instance is not present (not initialized) yet
         */
        private static Object getObject(final String className) {
            final Object inst = INSTANCES.get(className);
            if (inst == null) throw new IllegalStateException(
                    "'" + className + "' not initialized yet"
            );
            return inst;
        }

        private static final Map<String, Object> INSTANCES = new HashMap<>();
    }

    /**
     * Returns the Bestium plugin instance.
     *
     * @return the {@link JavaPlugin} instance
     * @throws IllegalStateException if called before the plugin is loaded
     */
    static JavaPlugin getPlugin() {
        return (JavaPlugin) InstanceHolder.getObject("cz.jeme.bestium.BestiumPlugin");
    }

    /**
     * Returns the Bestium plugin instance cast as {@link Bestium}.
     * <p>
     * This instance can be safely cast back to a {@link JavaPlugin}.
     *
     * @return the {@link Bestium} instance
     * @throws IllegalStateException if called before the plugin is loaded
     */
    static Bestium getBestium() {
        return (Bestium) getPlugin();
    }

    /**
     * Returns the main entity injector instance, which handles the registration and management
     * of custom entities within Bestium.
     *
     * @return the injector instance
     * @throws IllegalStateException if called before the Bestium bootstrap happens
     * @see EntityInjector
     */
    static EntityInjector getInjector() {
        return (EntityInjector) InstanceHolder.getObject("cz.jeme.bestium.EntityInjectorImpl");
    }

    /**
     * Returns the plugin support instance, providing access to functions
     * for checking the status of external optional dependencies.
     *
     * @return the plugin support instance
     * @throws IllegalStateException if called before Bestium plugin is loaded
     * @see PluginSupport
     */
    static PluginSupport getPluginSupport() {
        return (PluginSupport) InstanceHolder.getObject("cz.jeme.bestium.PluginSupportImpl");
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
    static <T extends Entity> T spawn(
            final Location location,
            final Class<T> entityClass,
            final EntitySpawnReason entitySpawnReason,
            final CreatureSpawnEvent.SpawnReason creatureSpawnReason,
            final Consumer<T> consumer
    ) {
        final EntityType<Entity> type = getInjector().getTypes().get(entityClass);
        if (type == null) throw new IllegalArgumentException(
                "Entity '" + entityClass + "' is not an injected entity!"
        );

        @SuppressWarnings("unchecked") final T entity = (T) type.spawn(
                ((CraftWorld) location.getWorld()).getHandle(),
                e -> {
                    // update location to be more precise
                    e.snapTo(
                            location.x(),
                            location.y(),
                            location.z(),
                            e.getYRot(),
                            e.getXRot()
                    );
                    consumer.accept((T) e);
                },
                CraftLocation.toBlockPosition(location),
                entitySpawnReason,
                false,
                false,
                creatureSpawnReason
        );
        return entity;
    }

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
    static <T extends Entity> T spawn(
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
                e -> {
                }
        );
    }

    /**
     * Retrieves the Bestium {@link Key} of the injected Bukkit {@link org.bukkit.entity.Entity}, or {@code null} if not injected.
     * <p>
     * This is the same key that was used when registering the entity injection.
     *
     * @param entity the Bukkit {@link org.bukkit.entity.Entity} to retrieve the key from
     * @return the {@link Key} identifying the injected entity type, or {@code null} if the entity is not an injected entity
     * @see #requireInjectedEntityKey(org.bukkit.entity.Entity)
     */
    @SuppressWarnings("PatternValidation")
    static @Nullable Key getInjectedEntityKey(final org.bukkit.entity.Entity entity) {
        String keyStr = entity.getPersistentDataContainer().get(
                Injectable.BESTIUM_ID_KEY,
                PersistentDataType.STRING
        );
        return keyStr == null ? null : Key.key(keyStr);
    }

    /**
     * Retrieves the Bestium {@link Key} of the injected Bukkit {@link org.bukkit.entity.Entity}.
     * <p>
     * This is the same key that was used when registering the entity injection.
     * Throws an exception if the entity is not injected.
     *
     * @param entity the Bukkit {@link org.bukkit.entity.Entity} to retrieve the key from
     * @return the {@link Key} identifying the injected entity type
     * @throws IllegalArgumentException if the entity is not an injected entity
     * @see #getInjectedEntityKey(org.bukkit.entity.Entity)
     */
    static Key requireInjectedEntityKey(final org.bukkit.entity.Entity entity) {
        final Key key = getInjectedEntityKey(entity);
        if (key == null) throw new IllegalArgumentException(
                "Provided entity is not an injected entity!"
        );
        return key;
    }

    /**
     * Checks whether the given Bukkit {@link org.bukkit.entity.Entity} is an injected entity.
     *
     * @param entity the Bukkit {@link org.bukkit.entity.Entity} to check
     * @return {@code true} if the entity was injected by Bestium, {@code false} otherwise
     */
    static boolean isInjectedEntity(final org.bukkit.entity.Entity entity) {
        return entity.getPersistentDataContainer().has(
                Injectable.BESTIUM_ID_KEY,
                PersistentDataType.STRING
        );
    }

    /**
     * Constructs a {@link NamespacedKey} from the given string key value.
     *
     * @param key the string value of the key. Must follow the {@link KeyPattern.Value} format
     * @return a {@link NamespacedKey} constructed from the given value
     */
    @ApiStatus.Internal
    NamespacedKey createKey(final @KeyPattern.Value String key);
}
