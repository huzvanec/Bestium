package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.inject.EntityInjection;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.apache.commons.lang3.DoubleRange;
import org.bukkit.Location;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * A functional interface with one method used to determine which
 * variant an entity will have after spawning.
 * <p>
 * Before implementing this interface, check the static factory methods,
 * a suitable implementation may already exist.
 *
 * @see #first()
 * @see #random()
 * @see #randomWithChance(Map)
 * @see #byBiomeKey(Map, String)
 * @see #byWorldKey(Map, String)
 * @see #byBiomeTemperature(Map, String)
 */
@FunctionalInterface
public interface VariantPicker {
    /**
     * Picks the variant to apply to an entity upon spawning.
     * <p>
     * The returned {@link BoundEntityVariant} must be one of the values in the provided {@code variants} map.
     * If a variant is returned that does not exist in this map, an exception will be thrown.
     * <p>
     * The {@code variants} map may be empty.
     * <p>
     * Only the data exposed by the {@code context} should be used in this method.
     * Accessing other properties of the entity may be unsafe at this stage.
     *
     * @param variants an unmodifiable map of variant IDs to bound variants
     * @param context  a safe snapshot of contextual data from the spawning entity
     * @return the variant to apply, or {@code null} if no variant (model) should be applied
     */
    @Nullable
    BoundEntityVariant pick(
            final @Unmodifiable Map<String, BoundEntityVariant> variants,
            final Context context
    );

    /**
     * A context object providing safe values from the entity at spawn time.
     * <p>
     * This should be the only source of data for the logic
     * within {@link VariantPicker#pick(Map, Context)}.
     */
    class Context {
        private final Location location;
        private final EntityType<?> realEntityType;
        private final EntityInjection<?, ?> injection;

        /**
         * Creates a context from the given entity.
         * <p>
         * This constructor is intended for internal use only.
         *
         * @param entity     the entity to create context for
         * @param entityType the internal Bestium entity type
         * @param injection  the {@link EntityInjection} used to inject the entity
         */
        @ApiStatus.Internal
        public Context(final Entity entity,
                       final EntityType<?> entityType,
                       final EntityInjection<?, ?> injection) {
            final var bukkit = entity.getBukkitEntity();
            location = bukkit.getLocation();
            this.realEntityType = entityType;
            this.injection = injection;
        }

        /**
         * Returns the location where the entity is spawning.
         * <p>
         * You may also access the world via {@link Location#getWorld()}.
         *
         * @return the spawn location of the entity
         */
        public Location getLocation() {
            return location;
        }

        /**
         * Returns the synthetic EntityType used internally.
         *
         * @return the internal Bestium entity type
         */
        public EntityType<?> getRealEntityType() {
            return realEntityType;
        }

        /**
         * Returns the entity's {@link EntityInjection}.
         *
         * @return the injection used to inject this entity
         */
        public EntityInjection<?, ?> getInjection() {
            return injection;
        }

        /**
         * Returns the backing Minecraft entity type.
         *
         * @return the backing type
         */
        public EntityType<?> getBackingEntityType() {
            return injection.getBackingType();
        }
    }

    /**
     * Returns a picker that selects the first variant from the provided map.
     * If no variants are available, returns {@code null}.
     * <p>
     * This is the default {@link VariantPicker} used when no picker is provided
     * during entity injection.
     *
     * @return a picker that always selects the first available variant
     */
    static VariantPicker first() {
        return VariantPickers.FIRST;
    }

    /**
     * Returns a picker that randomly selects one of the available variants.
     * If no variants are available, returns {@code null}.
     *
     * @return a picker that selects a random variant
     */
    static VariantPicker random() {
        return VariantPickers.random();
    }


    /**
     * Returns a picker that selects a variant based on weighted chances.
     * <p>
     * For example:
     * <pre>{@code
     * VariantPicker.randomWithChance(Map.of(
     *     "normal", 10,
     *     "rare", 1
     * ));
     * }</pre>
     * In this case, the {@code "normal"} variant is 10× more likely to be selected than {@code "rare"}.
     * Only the variants explicitly listed in the chance map may be selected. Others are ignored.
     * <p>
     * If no variants are available returns {@code null}.
     *
     * @param chanceMap a map of variant IDs to their selection weight (must be positive integers)
     * @return a chance based picker
     * @throws IllegalArgumentException if the picker attempts to pick a variant ID that does not exist
     */
    static VariantPicker randomWithChance(final Map<String, @Positive Integer> chanceMap) {
        return VariantPickers.randomWithChance(chanceMap);
    }

    /**
     * Returns a picker that selects a variant based on the biome key of the entity's spawn location.
     * <p>
     * For example:
     * <pre>{@code
     * VariantPicker.byBiomeKey(Map.of(
     *     "icy", Set.of(Key.key("minecraft:ice_spikes"), Key.key("minecraft:snowy_plains")),
     *     "mushroomy", Set.of(Key.key("mushroom_fields")),
     *     "yellow", Set.of(Key.key("my_plugin:yellowstone"))
     * ), "normal");
     * }</pre>
     * <p>
     * You may omit the {@code minecraft:} namespace for vanilla biomes.
     *
     * @param biomeMap a map of variant IDs to sets of biome keys
     * @param fallback the fallback variant ID to use if no match is found; may be {@code null}
     * @return a biome based picker
     * @throws IllegalArgumentException if the picker attempts to pick a variant ID that does not exist
     */
    static VariantPicker byBiomeKey(final Map<String, Set<Key>> biomeMap, final @Nullable String fallback) {
        return VariantPickers.byBiomeKey(biomeMap, fallback);
    }

    /**
     * Returns a picker that selects a variant based on the world key where the entity is spawning.
     * <p>
     * For example:
     * <pre>{@code
     * VariantPicker.byWorldKey(Map.of(
     *     "lava", Set.of(Key.key("minecraft:the_nether")),
     *     "aqua", Set.of(Key.key("my_plugin:water_world"))
     * ), "normal");
     * }</pre>
     * You may omit the {@code minecraft:} namespace for vanilla world keys.
     *
     * @param keyMap   a map of variant IDs to sets of world keys
     * @param fallback the fallback variant ID to use if no match is found; may be {@code null}
     * @return a world based picker
     */
    static VariantPicker byWorldKey(final Map<String, Set<Key>> keyMap, final @Nullable String fallback) {
        return VariantPickers.byWorldKey(keyMap, fallback);
    }

    /**
     * Returns a picker that selects a variant based on the biome temperature where the entity spawned.
     * <p>
     * For example:
     * <pre>{@code
     * VariantPicker.byBiomeTemperature(Map.of(
     *     "cold", DoubleRange.of(Double.MIN_VALUE, 0.0),
     *     "warm", DoubleRange.of(0.8, Double.MAX_VALUE)
     * ), "normal");
     * }</pre>
     * <p>
     * At the time of writing, the vanilla Minecraft biome temperature ranges from {@code -0.7} to {@code 2.0}, but
     * this may change with the addition of new and custom biomes.
     * <p>
     * For more information about biome temperature, see <a href="https://minecraft.wiki/w/Biome#Temperature">Minecraft Wiki: Biome Temperature</a>
     *
     * @param temperatureMap a map of variant IDs to temperature ranges
     * @param fallback       the fallback variant ID to use if no match is found; may be {@code null}
     * @return a temperature based picker
     */
    static VariantPicker byBiomeTemperature(final Map<String, DoubleRange> temperatureMap, final @Nullable String fallback) {
        return VariantPickers.byBiomeTemperature(temperatureMap, fallback);
    }
}
