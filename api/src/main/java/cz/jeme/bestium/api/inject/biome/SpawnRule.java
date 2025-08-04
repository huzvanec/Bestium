package cz.jeme.bestium.api.inject.biome;

import cz.jeme.bestium.api.inject.EntityInjection;
import net.kyori.adventure.key.Key;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.DoubleRange;
import org.jspecify.annotations.Nullable;

/**
 * A functional interface with one method used to determine in what quantities and how often
 * an entity will spawn in a certain {@link Biome}.
 * <p>
 * Before implementing this interface, check the static factory methods,
 * a suitable implementation may already exist.
 * <p>
 * To combine multiple variant rules into one, see {@link #firstMatch(SpawnRule...)}.
 */
@FunctionalInterface
public interface SpawnRule {
    /**
     * Called once for each biome during server startup.
     * <p>
     * This method determines how frequently and in what group sizes a mob should spawn in the given biome
     * by returning a {@link SpawnData} instance. If {@code null} is returned, the mob will not spawn in that biome.
     *
     * @param biome the biome to evaluate
     * @return spawn settings for the mob in this biome, or {@code null} if the mob should not spawn here
     */
    @Nullable
    SpawnData apply(final Biome biome);

    /**
     * A {@link SpawnRule} that prevents the entity from spawning in any biome.
     * <p>
     * Always returns {@code null} when evaluated, indicating that the entity should not spawn.
     * This is the default rule applied when building an {@link EntityInjection}.
     *
     * @return a spawn rule that disables natural spawning for the entity
     */
    static SpawnRule never() {
        return SpawnRules.NEVER;
    }

    /**
     * Returns the provided {@link SpawnData} if the given {@link BiomeFilter} matches the current biome.
     * <p>
     * This allows you to conditionally enable spawning in specific biomes using predefined filters,
     * such as {@link BiomeFilter#overworld()}, {@link BiomeFilter#tag(Key)}, or {@link BiomeFilter#baseTemperature(DoubleRange)}.
     *
     * @param filter    the biome filter used to determine where the entity should spawn
     * @param spawnData the spawn data to return if the biome matches the filter
     * @return a spawn rule that applies the given spawn data when the biome matches
     */
    static SpawnRule ifBiome(final BiomeFilter filter, final SpawnData spawnData) {
        return SpawnRules.ifBiome(filter, spawnData);
    }

    /**
     * Combines multiple spawn rules into a single rule by evaluating them in order, from first to last.
     * <p>
     * The first rule that returns a non-null {@link SpawnData} will be used. All subsequent
     * rules are ignored. This creates layered fallback logic where more specific rules are prioritized.
     *
     * @param rules the spawn rules to evaluate in order
     * @return a {@link SpawnRule} returning the first non-null {@link SpawnData} or {@code null} if none
     * is provided
     */
    static SpawnRule firstMatch(final SpawnRule... rules) {
        return SpawnRules.firstMatch(rules);
    }
}
