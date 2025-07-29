package cz.jeme.bestium.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * Provides support for checking the status of optional dependency plugins for Bestium.
 */
@ApiStatus.NonExtendable
public interface PluginSupport {

    /**
     * Checks if the BetterModel plugin is successfully loaded and enabled on the server.
     *
     * @return {@code true} if BetterModel is loaded and enabled, {@code false} otherwise
     */
    boolean betterModel();

    /**
     * Checks if the Nova plugin is successfully loaded and enabled on the server.
     *
     * @return {@code true} if BetterModel is loaded and enabled, {@code false} otherwise
     */
    boolean nova();
}
