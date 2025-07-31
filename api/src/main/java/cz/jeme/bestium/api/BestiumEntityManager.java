package cz.jeme.bestium.api;

import cz.jeme.bestium.api.inject.Injectable;
import cz.jeme.bestium.api.inject.variant.VariantPicker;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;


/**
 * Handles the initialization, lifecycle and other logic and helper methods for Bestium entities.
 *
 * @see #isInjectedEntity(Entity)
 * @see #getInjectedEntityKey(Entity)
 * @see #requireInjectedEntityKey(Entity)
 */
@ApiStatus.NonExtendable
public interface BestiumEntityManager {
    /**
     * Initializes the internals for a given Bestium entity.
     * <p>
     * This method is called from {@link Injectable#initBestium(EntityType, Level)}
     * to set up variant selection, persistent data, model rendering,
     * and possible data migration.
     * <p>
     * It is called exactly once during the construction of an entity
     * that uses the Bestium injection system.
     *
     * @param injectable the entity implementing {@link Injectable}
     * @param entityType the real entity type
     * @param level      the level the entity is spawning or being loaded into
     * @throws IllegalArgumentException if the provided {@code entityType} does not match the injected one
     * @throws IllegalStateException    if the {@link VariantPicker} returns a variant for the wrong entity
     */
    @ApiStatus.Internal
    void initializeBestiumEntity(
            final Injectable injectable,
            final EntityType<?> entityType,
            final Level level
    );

    /**
     * Writes additional entity data to persistent storage.
     *
     * @param injectable the entity being saved
     * @param output     the save target to write to
     */
    @ApiStatus.Internal
    void saveBestiumEntity(final Injectable injectable, final ValueOutput output);

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
