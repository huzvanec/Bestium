package cz.jeme.bestium.api.inject.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public record SpawnData(
        int weight,
        int minGroupSize,
        int maxGroupSize,
        @Nullable MobCost cost
) {
    public SpawnData(final int weight,
                     final int minGroupSize,
                     final int maxGroupSize) {
        this(weight, minGroupSize, maxGroupSize, null);
    }

    public SpawnData(final int weight,
                     final int groupSize) {
        this(weight, groupSize, groupSize);
    }

    public record MobCost(double charge, double energyBudget) {
        @ApiStatus.Internal
        public MobSpawnSettings.MobSpawnCost toMobSpawnCost() {
            return new MobSpawnSettings.MobSpawnCost(charge, energyBudget);
        }
    }

    @ApiStatus.Internal
    public MobSpawnSettings.SpawnerData toSpawnerData(final EntityType<?> type) {
        return new MobSpawnSettings.SpawnerData(type, minGroupSize, maxGroupSize);
    }
}
