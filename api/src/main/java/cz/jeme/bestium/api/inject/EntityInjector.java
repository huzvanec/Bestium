package cz.jeme.bestium.api.inject;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

/**
 * Responsible for injecting custom entities into the Minecraft server runtime.
 * Handles registration of custom {@link Entity} types, assigns attributes,
 * registers them into both the Minecraft and Bukkit registries, and ensures proper
 * data fixer and type mapping.
 * <p>
 * After injection, the system becomes frozen and no further entities can be registered.
 */
@NullMarked
public interface EntityInjector {
    /**
     * Registers an entity by building it from the provided builder.
     * <p>
     * This is a convenience method equivalent to:
     * <pre>{@code
     * register(builder.build());
     * }</pre>
     *
     * @param builder the builder used to construct and register the entity injection
     * @throws IllegalStateException    if the injector is already frozen and no more entities can be registered
     * @throws IllegalArgumentException if the entity has already been registered for injection
     * @see #frozen()
     * @see EntityInjection
     */
    default void register(final EntityInjection.Builder<?> builder) {
        register(builder.build());
    }

    /**
     * Registers a prepared entity injection for later application.
     * <p>
     * Should be called before the injection phase is finalized.
     *
     * @param injection the entity injection to register
     * @throws IllegalStateException    if the injector is already frozen and no more entities can be registered
     * @throws IllegalArgumentException if an entity with the same class or key as this injection has already been registered for injection
     * @see #frozen()
     * @see EntityInjection
     */
    void register(final EntityInjection<?> injection);

    /**
     * Indicates whether the injection phase has been completed.
     * <p>
     * When frozen, no further entities may be registered and the injector
     * can no longer be used to modify the runtime.
     * <p>
     * The injection phase always runs before worlds are loaded. This ensures
     * that all custom entities are properly registered before the server attempts
     * to load them from disk, preventing data corruption due to
     * unrecognized entity types.
     *
     * @return {@code true} if the injector has been frozen, {@code false} otherwise
     */
    boolean frozen();

    /**
     * Returns an unmodifiable view of all registered entity injections.
     * <p>
     * The returned map contains entity classes as keys and their corresponding
     * {@link EntityInjection} instances as values.
     *
     * @param <T> the entity type
     * @return an unmodifiable map of entity classes to their registered injections
     */
    <T extends Entity & Injectable> @Unmodifiable Map<Class<T>, EntityInjection<T>> injections();
}
