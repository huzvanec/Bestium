package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Extend this class to implement your own tamable animal with custom behavior or properties.
 * <p>
 * Examples of vanilla {@link TamableAnimal} subclasses: {@link Cat}, {@link Wolf}
 */
public abstract class CustomTamableAnimal extends TamableAnimal implements Injectable {
    /**
     * The constructor of this class.
     *
     * @param entityType the real type of this entity, unlike {@link #getType()}
     * @param level      the level this entity is created in
     */
    protected CustomTamableAnimal(final EntityType<? extends CustomTamableAnimal> entityType,
                                  final Level level) {
        super(entityType, level);
        initBestium(entityType, level);
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
