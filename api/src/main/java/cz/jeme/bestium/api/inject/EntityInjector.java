package cz.jeme.bestium.api.inject;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Responsible for injecting custom entities into the Minecraft server runtime.
 * Handles registration of custom {@link Entity} types, assigns attributes,
 * registers them into both the Minecraft and Bukkit registries, and ensures proper
 * data fixer and type mapping.
 * <p>
 * After registration, the system becomes frozen and no further entities can be registered.
 *
 * @see #register(Supplier)
 * @see #canRegister()
 */
@NullMarked
public interface EntityInjector {
    /**
     * Registers a prepared entity injection for later application.
     * <p>
     * Should be called before the registration phase ends.
     *
     * @param injectionSupplier supplier that provides the entity injection to register
     * @throws IllegalStateException if the injector is already frozen and no more entities can be registered
     * @see EntityInjection
     * @see #canRegister()
     */
    void register(final Supplier<EntityInjection<?, ?>> injectionSupplier);

    /**
     * Returns an unmodifiable view of all registered entity injections.
     * <p>
     * The returned map contains entity classes as keys and their corresponding
     * {@link EntityInjection} instances as values.
     *
     * @param <T> the entity type
     * @return an unmodifiable map of entity classes to their registered injections
     * @throws IllegalStateException if the injector is still in the {@link Phase#REGISTRATION} or {@link Phase#PRE_INJECTION} phase
     * @see #phase()
     */
    <T extends Entity & Injectable> @Unmodifiable Map<Class<T>, EntityInjection<T, ?>> injections();

    /**
     * Returns an unmodifiable view of all generated entity types.
     * <p>
     * The returned map contains entity classes as keys and their corresponding
     * {@link EntityType} instances as values.
     *
     * @param <T> the entity type
     * @return an unmodifiable map of entity classes to their generated entity types
     * @throws IllegalStateException if the injector is in any other than the {@link Phase#INJECTED} phase
     * @see #phase()
     */
    <T extends Entity & Injectable> @Unmodifiable Map<Class<T>, EntityType<T>> types();

    /**
     * Returns the current phase of the entity injection lifecycle.
     *
     * @return the current {@link Phase}
     */
    Phase phase();

    /**
     * Helper method that returns whether injection registrations are still allowed.
     *
     * @return {@code true} if the injector is currently in the {@link Phase#REGISTRATION} phase, {@code false} otherwise
     */
    default boolean canRegister() {
        return phase() == Phase.REGISTRATION;
    }

    /**
     * Represents the lifecycle phases of the entity injection process.
     * <p>
     * The injector transitions through these phases in order.
     */
    enum Phase {
        /**
         * The injector is open to entity registration.
         * Plugins may provide their registrations via {@link #register(Supplier)}.
         */
        REGISTRATION,
        /**
         * Registration is closed, all suppliers are resolved and stored.
         * No further registrations are allowed.
         */
        PRE_INJECTION,
        /**
         * Phase 1: inject into Minecraft internals.
         */
        INJECTION_PHASE_1,
        /**
         * Phase 2: inject into Bukkit internals.
         */
        INJECTION_PHASE_2,
        /**
         * All phases complete, entities are fully injected.
         */
        INJECTED
    }
}
