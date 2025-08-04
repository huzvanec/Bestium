package cz.jeme.bestium.api.inject.biome;

final class SpawnRules {
    private SpawnRules() {
        throw new AssertionError();
    }

    public static final SpawnRule NEVER = (biome) -> null;

    public static SpawnRule ifBiome(final BiomeFilter filter, final SpawnData spawnData) {
        return biome -> filter.test(biome) ? spawnData : null;
    }

    public static SpawnRule firstMatch(final SpawnRule... rules) {
        return biome -> {
            for (final SpawnRule rule : rules) {
                final SpawnData data = rule.apply(biome);
                if (data != null) return data;
            }
            return null;
        };
    }
}
