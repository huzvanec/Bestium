package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own aging mob with custom behavior or properties.
 * <p>
 * Before directly extending this class, you should consider extending {@link CustomAgeableWaterCreature},
 * {@link CustomAnimal} or {@link CustomAbstractVillager}.
 */
@NullMarked
public abstract class CustomAgeableMob extends AgeableMob implements Injectable {
    protected CustomAgeableMob(final EntityType<? extends CustomAgeableMob> entityType,
                               final Level level) {
        super(entityType, level);
        initBestium();
    }

    @Override
    public final EntityType<?> getType() {
        return getBestiumBackingType();
    }

    @Override
    public void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);
        addBestiumAdditionalSaveData(output);
    }
}
