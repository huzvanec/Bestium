package cz.jeme.bestium.api;

import cz.jeme.bestium.api.inject.EntityInjector;
import net.kyori.adventure.key.KeyPattern;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * The core system for managing custom Minecraft entities.
 */
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
        private static Object getObject(final String className) {
            final Object inst = INSTANCES.get(className);
            if (inst == null) throw new IllegalStateException(
                    "'" + className + "' not initialized yet"
            );
            return inst;
        }

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
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
     * @see EntityInjector
     */
    static EntityInjector getInjector() {
        return (EntityInjector) InstanceHolder.getObject("cz.jeme.bestium.inject.EntityInjectorImpl");
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
     * Returns the Bestium entity manager instance, handling the initialization,
     * lifecycle and other logic and helper methods for Bestium entities.
     *
     * @return the entity manager instance
     * @see EntityManager
     */
    static EntityManager getEntityManager() {
        return (EntityManager) Bestium.InstanceHolder.getObject("cz.jeme.bestium.EntityManagerImpl");
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
