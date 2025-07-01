package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own living entity with custom behavior or properties.
 * <p>
 * Before directly extending this class, you should consider extending {@link CustomMob}.
 * <p>
 * Examples of vanilla {@link LivingEntity} subclasses: {@link ArmorStand}
 */
@NullMarked
public abstract class CustomLivingEntity extends LivingEntity implements Injectable {
    protected CustomLivingEntity(final EntityType<? extends CustomLivingEntity> entityType,
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
