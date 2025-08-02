package cz.jeme.bestium.api.inject.biome;

import net.kyori.adventure.key.Key;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.DoubleRange;

import java.util.Set;

@FunctionalInterface
public interface BiomeFilter {
    boolean test(final Biome biome);

    static BiomeFilter and(final BiomeFilter... filters) {
        return BiomeFilters.and(filters);
    }

    static BiomeFilter or(final BiomeFilter... filters) {
        return BiomeFilters.or(filters);
    }

    static BiomeFilter not(final BiomeFilter filter) {
        return BiomeFilters.not(filter);
    }

    static BiomeFilter temperature(final DoubleRange temperatureRange) {
        return BiomeFilters.temperature(temperatureRange);
    }

    static BiomeFilter tag(final Key tagKey) {
        return BiomeFilters.tag(tagKey);
    }

    static BiomeFilter key(final Key biomeKey) {
        return keys(Set.of(biomeKey));
    }

    static BiomeFilter keys(final Set<Key> biomeKeys) {
        return BiomeFilters.keys(biomeKeys);
    }

    static BiomeFilter overworld() {
        return tag(Key.key("minecraft:is_overworld"));
    }

    static BiomeFilter nether() {
        return tag(Key.key("minecraft:is_nether"));
    }

    static BiomeFilter end() {
        return tag(Key.key("minecraft:is_end"));
    }
}
