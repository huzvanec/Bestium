package cz.jeme.bestium.api.inject.biome;

import net.minecraft.world.level.biome.Biome;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface SpawnRule {
    @Nullable
    SpawnData apply(final Biome biome);

    static SpawnRule none() {
        return SpawnRules.NONE;
    }

    static SpawnRule ifBiome(final BiomeFilter filter, final SpawnData spawnData) {
        return SpawnRules.ifBiome(filter, spawnData);
    }

    static SpawnRule firstMatch(final SpawnRule... rules) {
        return SpawnRules.firstMatch(rules);
    }
}
