package cz.jeme.bestium.api.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class CustomMonster extends Monster implements InjectableEntity {
    protected CustomMonster(final EntityType<? extends CustomMonster> entityType,
                            final Level level) {
        super(entityType, level);
        bestium$init();
    }

    @Override
    public final EntityType<?> getType() {
        return bestium$getType();
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        bestium$addAdditionalSaveData(compound);
    }
}
