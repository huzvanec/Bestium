package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class CustomEntity extends Entity implements Injectable {
    public CustomEntity(final EntityType<? extends CustomEntity> entityType, final Level level) {
        super(entityType, level);
        bestium_init();
    }

    @Override
    public final EntityType<?> getType() {
        return bestium_getType();
    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag compound) {
        bestium_addAdditionalSaveData(compound);
    }
}
