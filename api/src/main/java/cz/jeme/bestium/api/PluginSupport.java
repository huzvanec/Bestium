package cz.jeme.bestium.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * Provides support for checking the status of optional dependency plugins for Bestium.
 */
@ApiStatus.NonExtendable
public interface PluginSupport {
    /**
     * Returns the plugin support instance, providing access to functions
     * for checking the status of external optional dependencies.
     *
     * @return the plugin support instance
     * @throws IllegalStateException if called before Bestium plugin is loaded
     */
    static PluginSupport getSupport() {
        return Bestium.getPluginSupport();
    }

    /**
     * Checks if the BetterModel plugin is successfully loaded and enabled on the server.
     *
     * @return {@code true} if BetterModel is loaded and enabled, {@code false} otherwise
     */
    boolean isBetterModelLoaded();

    /**
     * Checks if the Nova plugin is successfully loaded and enabled on the server.
     *
     * @return {@code true} if Nova is loaded and enabled, {@code false} otherwise
     */
    boolean isNovaLoaded();
}
