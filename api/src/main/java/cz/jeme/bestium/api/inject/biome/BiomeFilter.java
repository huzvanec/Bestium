package cz.jeme.bestium.api.inject.biome;

import cz.jeme.bestium.api.util.BiomeTemperature;
import net.kyori.adventure.key.Key;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.DoubleRange;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A functional interface representing a predicate (boolean test) for {@link Biome}s,
 * used to determine whether a biome satisfies specific criteria.
 * <p>
 * Filters can be composed using logical operations like {@link #and}, {@link #or}, or {@link #not}.
 */
@FunctionalInterface
public interface BiomeFilter {

    /**
     * Tests whether the given {@link Biome} matches the criteria defined by this filter.
     *
     * @param biome the biome to test
     * @return {@code true} if the biome matches the filter, {@code false} otherwise
     */
    boolean test(final Biome biome);

    /**
     * Creates a filter that matches only biomes which match all the provided filters.
     *
     * @param filters the filters to combine with a logical AND
     * @return a filter that matches biomes satisfying all given filters
     */
    static BiomeFilter and(final BiomeFilter... filters) {
        return BiomeFilters.and(filters);
    }

    /**
     * Creates a filter that matches biomes which match at least one of the provided filters.
     *
     * @param filters the filters to combine with a logical OR
     * @return a filter that matches biomes satisfying any of the given filters
     */
    static BiomeFilter or(final BiomeFilter... filters) {
        return BiomeFilters.or(filters);
    }

    /**
     * Creates a filter that matches biomes that do not satisfy the given filter.
     *
     * @param filter the filter to negate
     * @return a filter that matches the logical negation of the given filter
     */
    static BiomeFilter not(final BiomeFilter filter) {
        return BiomeFilters.not(filter);
    }

    /**
     * Creates a filter that matches biomes with a base temperature within the given range.
     * <p>
     * You may also use the predefined ranges in {@link BiomeTemperature}.
     *
     * @param temperatureRange the inclusive temperature range to match
     * @return a filter matching biomes with a base temperature in the given range
     * @see BiomeTemperature#COLD
     * @see BiomeTemperature#TEMPERATE
     * @see BiomeTemperature#WARM
     */
    static BiomeFilter baseTemperature(final DoubleRange temperatureRange) {
        return BiomeFilters.baseTemperature(temperatureRange);
    }

    /**
     * Creates a filter that matches biomes assigned to the specified biome tag.
     *
     * @param tagKey the key of the tag to match (e.g., {@code minecraft:is_savanna})
     * @return a filter that matches biomes tagged with the given key
     * @see <a href="https://minecraft.wiki/w/Biome_tag_(Java_Edition)">Minecraft Wiki - Biome tag</a>
     */
    static BiomeFilter tag(final Key tagKey) {
        return BiomeFilters.tag(tagKey);
    }

    /**
     * Creates a filter that matches exactly one biome, identified by the given biome key.
     *
     * @param biomeKey the key of the biome to match
     * @return a filter that matches only the specified biome
     * @see <a href="https://minecraft.wiki/w/Biome#Java_Edition">Minecraft Wiki - Biome IDs</a>
     */
    static BiomeFilter key(final Key biomeKey) {
        return keys(Set.of(biomeKey));
    }

    /**
     * Creates a filter that matches any biome whose key is included in the provided set.
     *
     * @param biomeKeys the set of biome keys to match
     * @return a filter that matches any biome in the given set
     * @see <a href="https://minecraft.wiki/w/Biome#Java_Edition">Minecraft Wiki - Biome IDs</a>
     */
    static BiomeFilter keys(final Set<Key> biomeKeys) {
        return BiomeFilters.keys(biomeKeys);
    }

    /**
     * Creates a filter that matches any biome whose key is included in the provided array of keys.
     *
     * @param first  the first biome keys to match
     * @param second the second biome keys to match
     * @param rest   the rest of the  biome keys to match
     * @return a filter that matches any biome in the given array of keys
     * @see <a href="https://minecraft.wiki/w/Biome#Java_Edition">Minecraft Wiki - Biome IDs</a>
     */
    static BiomeFilter keys(final Key first, final Key second, final Key... rest) {
        final HashSet<Key> biomeKeys = new HashSet<>();
        biomeKeys.add(first);
        biomeKeys.add(second);
        Collections.addAll(biomeKeys, rest);
        return keys(biomeKeys);
    }

    /**
     * Creates a filter that matches biomes tagged with {@code minecraft:is_overworld}.
     * <p>
     * <strong>Note:</strong> This filter will not match most datapack biomes.
     *
     * @return a filter for vanilla overworld biomes
     */
    static BiomeFilter overworld() {
        return tag(Key.key("minecraft:is_overworld"));
    }

    /**
     * Creates a filter that matches biomes tagged with {@code minecraft:is_nether}.
     * <p>
     * <strong>Note:</strong> This filter will not match most datapack biomes.
     *
     * @return a filter for vanilla Nether biomes
     */
    static BiomeFilter nether() {
        return tag(Key.key("minecraft:is_nether"));
    }

    /**
     * Creates a filter that matches biomes tagged with {@code minecraft:is_end}.
     * <p>
     * <strong>Note:</strong> This filter will not match most datapack biomes.
     *
     * @return a filter for vanilla End biomes
     */
    static BiomeFilter end() {
        return tag(Key.key("minecraft:is_end"));
    }
}