package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own animal with custom behavior or properties.
 * <p>
 * Examples of vanilla {@link Animal} subclasses: {@link Sheep}, {@link Pig}, {@link Panda}
 */
@NullMarked
public abstract class CustomAnimal extends Animal implements Injectable {
    protected CustomAnimal(final EntityType<? extends CustomAnimal> entityType,
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
