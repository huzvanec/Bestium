package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own shoulder riding entity with custom behavior or properties.
 * <p>
 * Examples of vanilla {@link ShoulderRidingEntity} subclasses: {@link Parrot}
 */
@NullMarked
public abstract class CustomShoulderRidingEntity extends ShoulderRidingEntity implements Injectable {
    protected CustomShoulderRidingEntity(final EntityType<? extends CustomShoulderRidingEntity> entityType,
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
