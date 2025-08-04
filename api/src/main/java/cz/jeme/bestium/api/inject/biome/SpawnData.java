package cz.jeme.bestium.api.inject.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Configuration for how often and in what group sizes a mob should spawn within a biome.
 *
 * @param weight       Relative spawn probability. Higher values increase spawn frequency compared to other entities in the same biome.
 *                     To configure this accurately, compare values of other mobs in the biome.
 *                     For vanilla biomes, refer to: <a href="https://minecraft.wiki/w/Plains">Minecraft Wiki - Plains</a>.
 * @param minGroupSize Minimum number of mobs that can spawn together in a group.
 * @param maxGroupSize Maximum number of mobs that can spawn together in a group.
 * @param cost         Optional spawn cost. See {@link MobCost} for details.
 */
public record SpawnData(
        int weight,
        int minGroupSize,
        int maxGroupSize,
        @Nullable MobCost cost
) {
    /**
     * Creates spawn data without a mob cost.
     *
     * @param weight       spawn weight
     * @param minGroupSize minimum group size
     * @param maxGroupSize maximum group size
     */
    public SpawnData(final int weight,
                     final int minGroupSize,
                     final int maxGroupSize) {
        this(weight, minGroupSize, maxGroupSize, null);
    }

    /**
     * Creates spawn data with an exact group size (min and max will be the same).
     *
     * @param weight    spawn weight
     * @param groupSize fixed group size
     */
    public SpawnData(final int weight,
                     final int groupSize) {
        this(weight, groupSize, groupSize);
    }

    /**
     * Represents additional spawn constraints introduced in Minecraft 1.16 ("The Nether Update").
     * Mob cost prevents entities from spawning too close to each other by assigning a charge and an energy budget.
     *
     * <p>Each mob contributes its {@code charge} to nearby blocks (scaled by distance). When a new mob tries to spawn,
     * the cumulative charge of surrounding entities is calculated. If the total exceeds the {@code energyBudget}, the mob
     * is not allowed to spawn. This creates natural spacing between mobs.
     *
     * <p>For example, in vanilla Minecraft, endermen in warped forests use mob costs to spawn less densely.
     *
     * <p>See: <a href="https://minecraft.wiki/w/Mob_spawning#Spawn_costs">Minecraft Wiki - Spawn Costs</a>
     *
     * @param charge       how much charge this mob contributes to the spawn area
     * @param energyBudget maximum total charge a spawn location can tolerate before blocking the spawn
     */
    public record MobCost(double charge, double energyBudget) {
        /**
         * Converts this {@link MobCost} to a {@link MobSpawnSettings.MobSpawnCost}.
         *
         * @return the NMS representation of this mob cost
         */
        @ApiStatus.Internal
        public MobSpawnSettings.MobSpawnCost toMobSpawnCost() {
            return new MobSpawnSettings.MobSpawnCost(charge, energyBudget);
        }
    }

    /**
     * Converts this {@link SpawnData} into {@link MobSpawnSettings.SpawnerData}.
     *
     * @param type the entity type to bind this spawn data to
     * @return the NMS representation of this spawn data
     */
    @ApiStatus.Internal
    public MobSpawnSettings.SpawnerData toSpawnerData(final EntityType<?> type) {
        return new MobSpawnSettings.SpawnerData(type, minGroupSize, maxGroupSize);
    }
}