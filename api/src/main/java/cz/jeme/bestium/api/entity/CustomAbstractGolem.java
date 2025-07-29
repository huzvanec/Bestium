package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Extend this class to implement your own golem with custom behavior or properties.
 * <p>
 * Examples of vanilla {@link AbstractGolem} subclasses: {@link IronGolem}, {@link SnowGolem}
 */
public abstract class CustomAbstractGolem extends AbstractGolem implements Injectable {
    /**
     * The constructor of this class.
     *
     * @param entityType the real type of this entity, unlike {@link #getType()}
     * @param level      the level this entity is created in
     */
    protected CustomAbstractGolem(final EntityType<? extends CustomAbstractGolem> entityType,
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
