package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own fish with schooling and other custom behavior or properties.
 * <p>
 * Examples of vanilla {@link AbstractSchoolingFish} subclasses: {@link Cod}, {@link Salmon}
 */
@NullMarked
public abstract class CustomAbstractSchoolingFish extends AbstractSchoolingFish implements Injectable {
    protected CustomAbstractSchoolingFish(final EntityType<? extends CustomAbstractSchoolingFish> entityType,
                                          final Level level) {
        super(entityType, level);
        initBestium();
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
