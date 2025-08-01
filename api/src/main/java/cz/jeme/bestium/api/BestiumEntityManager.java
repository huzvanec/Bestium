package cz.jeme.bestium.api;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;


/**
 * Handles the post-initialization, lifecycle and other logic and helper methods for Bestium entities.
 *
 * @see #isInjectedEntity(Entity)
 * @see #getInjectedEntityKey(Entity)
 * @see #requireInjectedEntityKey(Entity)
 */
@ApiStatus.NonExtendable
public interface BestiumEntityManager {
    /**
     * Checks whether the given Bukkit {@link Entity} is an injected entity.
     *
     * @param entity the Bukkit {@link Entity} to check
     * @return {@code true} if the entity was injected by Bestium, {@code false} otherwise
     */
    boolean isInjectedEntity(final Entity entity);

    /**
     * Retrieves the Bestium {@link Key} of the injected Bukkit {@link Entity}, or {@code null} if not injected.
     * <p>
     * This is the same key that was used when registering the entity injection.
     *
     * @param entity the Bukkit {@link Entity} to retrieve the key from
     * @return the {@link Key} identifying the injected entity type, or {@code null} if the entity is not an injected entity
     * @see #requireInjectedEntityKey(Entity)
     */
    @Nullable
    Key getInjectedEntityKey(final Entity entity);

    /**
     * Retrieves the Bestium {@link Key} of the injected Bukkit {@link Entity}.
     * <p>
     * This is the same key that was used when registering the entity injection.
     * Throws an exception if the entity is not injected.
     *
     * @param entity the Bukkit {@link Entity} to retrieve the key from
     * @return the {@link Key} identifying the injected entity type
     * @throws IllegalArgumentException if the entity is not an injected entity
     * @see #getInjectedEntityKey(Entity)
     */
    default Key requireInjectedEntityKey(final Entity entity) {
        final Key key = getInjectedEntityKey(entity);
        if (key == null) throw new IllegalArgumentException(
                "Provided entity is not an injected entity!"
        );
        return key;
    }
}
