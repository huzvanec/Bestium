package cz.jeme.bestium.api.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class CustomAnimal extends Animal implements Injectable {
    protected CustomAnimal(final EntityType<? extends CustomAnimal> entityType,
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
