package cz.jeme.bestium.api.inject.biome;

import net.kyori.adventure.key.Key;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.apache.commons.lang3.DoubleRange;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

final class BiomeFilters {
    private BiomeFilters() {
        throw new AssertionError();
    }

    public static BiomeFilter and(final BiomeFilter... filters) {
        return biome -> {
            for (final BiomeFilter filter : filters) {
                if (!filter.test(biome)) return false;
            }
            return true;
        };
    }

    public static BiomeFilter or(final BiomeFilter... filters) {
        return biome -> {
            for (final BiomeFilter filter : filters) {
                if (filter.test(biome)) return true;
            }
            return false;
        };
    }

    public static BiomeFilter not(final BiomeFilter filter) {
        return biome -> !filter.test(biome);
    }

    private static RegistryAccess.Frozen registryAccess() {
        return ((CraftServer) Bukkit.getServer()).getHandle().getServer().registryAccess();
    }

    private static @Nullable Registry<Biome> lazyBiomeRegistry;

    private static Registry<Biome> biomeRegistry() {
        if (lazyBiomeRegistry == null) {
            lazyBiomeRegistry = registryAccess().lookupOrThrow(Registries.BIOME);
        }
        return lazyBiomeRegistry;
    }

    private static @Nullable Map<Key, TagKey<Biome>> lazyBiomeTags;

    @SuppressWarnings("PatternValidation")
    private static Map<Key, TagKey<Biome>> biomeTags() {
        if (lazyBiomeTags == null) {
            lazyBiomeTags = biomeRegistry().getTags()
                    .map(HolderSet.Named::key)
                    .collect(Collectors.toMap(
                            key -> Key.key(key.location().toString()),
                            Function.identity(),
                            (k1, k2) -> {
                                throw new AssertionError(
                                        "Duplicate tag keys encountered"
                                );
                            },
                            HashMap::new
                    ));
        }
        return lazyBiomeTags;
    }

    public static BiomeFilter tag(final Key tagKey) {
        return biome -> {
            final Identifier id = biomeRegistry().getKey(biome);
            assert id != null;
            return biomeRegistry()
                    .get(id)
                    .orElseThrow()
                    .is(biomeTags().get(tagKey));
        };
    }

    @SuppressWarnings("PatternValidation")
    public static BiomeFilter keys(final Set<Key> biomeKeys) {
        return biome -> {
            final Identifier id = biomeRegistry().getKey(biome);
            assert id != null;
            return biomeKeys.contains(Key.key(id.toString()));
        };
    }

    public static BiomeFilter baseTemperature(final DoubleRange temperatureRange) {
        return biome -> temperatureRange.contains((double) biome.getBaseTemperature());
    }
}
