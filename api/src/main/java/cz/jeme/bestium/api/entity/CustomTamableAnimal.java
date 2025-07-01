package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own tamable animal with custom behavior or properties.
 * <p>
 * Examples of vanilla {@link TamableAnimal} subclasses: {@link Cat}, {@link Wolf}
 */
@NullMarked
public abstract class CustomTamableAnimal extends TamableAnimal implements Injectable {
    protected CustomTamableAnimal(final EntityType<? extends CustomTamableAnimal> entityType,
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
