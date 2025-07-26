package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own entity with custom behavior or properties.
 * <p>
 * This class should be rarely used, extending it may lead to unstable behavior.
 * Examples of vanilla {@link Entity} subclasses: {@link LightningBolt}, {@link FallingBlock}
 */
@NullMarked
public abstract class CustomEntity extends Entity implements Injectable {
    protected CustomEntity(final EntityType<? extends CustomEntity> entityType, final Level level) {
        super(entityType, level);
        initBestium();
    }

    @Override
    public final EntityType<?> getType() {
        return getBestiumBackingType();
    }

    @Override
    protected void addAdditionalSaveData(final ValueOutput output) {
        addBestiumAdditionalSaveData(output);
    }
}
