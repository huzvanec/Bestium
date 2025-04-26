package cz.jeme.bestium.api.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class CustomEntity extends Entity implements InjectableEntity {
    public CustomEntity(final EntityType<? extends CustomEntity> entityType, final Level level) {
        super(entityType, level);
        bestium$init();
    }

    @Override
    public final EntityType<?> getType() {
        return bestium$getType();
    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag compound) {
        bestium$addAdditionalSaveData(compound);
    }
}
