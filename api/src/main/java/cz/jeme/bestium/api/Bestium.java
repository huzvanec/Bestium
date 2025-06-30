package cz.jeme.bestium.api;

import cz.jeme.bestium.api.inject.EntityInjector;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

/**
 * The core system for managing custom Minecraft entities.
 */
@NullMarked
@ApiStatus.NonExtendable
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
        private static Object get(final String className) {
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
    static JavaPlugin plugin() {
        return (JavaPlugin) InstanceHolder.get("cz.jeme.bestium.BestiumPlugin");
    }

    /**
     * Returns the main entity injector instance, which handles the registration and management
     * of custom entities within Bestium.
     *
     * @return the injector instance
     * @throws IllegalStateException if called before the Bestium bootstrap happens
     * @see EntityInjector
     */
    static EntityInjector injector() {
        return (EntityInjector) InstanceHolder.get("cz.jeme.bestium.EntityInjectorImpl");
    }

    /**
     * Returns the plugin support instance, providing access to functions
     * for checking the status of external optional dependencies.
     *
     * @return the plugin support instance
     * @throws IllegalStateException if called before Bestium plugin is loaded
     * @see PluginSupport
     */
    static PluginSupport pluginSupport() {
        return (PluginSupport) InstanceHolder.get("cz.jeme.bestium.PluginSupportImpl");
    }
}
