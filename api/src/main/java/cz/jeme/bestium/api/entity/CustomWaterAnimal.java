package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own water animal with custom behavior or properties.
 * <p>
 * Before directly extending this class, you should consider extending {@link CustomAbstractFish} or {@link CustomAbstractSchoolingFish}.
 */
@NullMarked
public abstract class CustomWaterAnimal extends WaterAnimal implements Injectable {
    protected CustomWaterAnimal(final EntityType<? extends CustomWaterAnimal> entityType,
                                final Level level) {
        super(entityType, level);
        bestium_init();
    }

    @Override
    public final EntityType<?> getType() {
        return bestium_backingType();
    }

    @Override
    public void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);
        bestium_addAdditionalSaveData(output);
    }
}
