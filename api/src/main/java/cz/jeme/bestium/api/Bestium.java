package cz.jeme.bestium.api;

import cz.jeme.bestium.api.inject.EntityInjector;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * The core system for managing custom Minecraft entities.
 */
@NullMarked
public interface Bestium {

    /**
     * Acquires the instance of the Bestium plugin.
     *
     * @return the instance of Bestium
     * @throws NullPointerException if the Bestium plugin is not successfully loaded on the server
     *                              and the instance could not be acquired
     */
    static Bestium instance() {
        return Objects.requireNonNull(
                InstanceHolder.bestium,
                "Bestium has not been initialized yet"
        );
    }

    /**
     * Holds the internal singleton instance of {@link Bestium}.
     * <p>
     * This class is not intended for public use.
     * To access the stored {@link Bestium} instance use {@link Bestium#instance()}.
     */
    @ApiStatus.Internal
    final class InstanceHolder {
        private InstanceHolder() {
            throw new AssertionError();
        }

        /**
         * Internal reference to the active {@link Bestium} instance.
         * <p>
         * This is set via reflection during plugin initialization and should not be accessed directly.
         */
        private static @Nullable Bestium bestium;
    }

    /**
     * Returns the main entity injector instance, which handles the registration and management
     * of custom entities within Bestium.
     *
     * @return the injector instance
     * @see EntityInjector
     */
    EntityInjector injector();

    /**
     * Returns the plugin support instance, providing access to functions
     * for checking the status of external optional dependencies.
     *
     * @return the plugin support instance
     * @see PluginSupport
     */
    PluginSupport pluginSupport();
}
