package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Extend this class to implement your own aging mob with custom behavior or properties.
 * <p>
 * Before directly extending this class, you should consider extending {@link CustomAgeableWaterCreature},
 * {@link CustomAnimal} or {@link CustomAbstractVillager}.
 */
public abstract class CustomAgeableMob extends AgeableMob implements Injectable {
    /**
     * The constructor of this class.
     *
     * @param entityType the real type of this entity, unlike {@link #getType()}
     * @param level      the level this entity is created in
     */
    protected CustomAgeableMob(final EntityType<? extends CustomAgeableMob> entityType,
                               final Level level) {
        super(entityType, level);
        bestium_init();
    }

    @Override
    public final EntityType<?> getType() {
        return bestium_getBackingType();
    }

    @Override
    public void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);
        bestium_addAdditionalSaveData(output);
    }
}
