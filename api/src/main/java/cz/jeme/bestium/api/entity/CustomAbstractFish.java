package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own fish with custom behavior or properties.
 * <p>
 * This base class does not include schooling behavior like that of cod, salmon, or tropical fish in Minecraft.
 * For schooling behavior, see {@link CustomAbstractSchoolingFish}.
 * <p>
 * Examples of vanilla {@link AbstractFish} subclasses: {@link Tadpole}, {@link Pufferfish}
 */
@NullMarked
public abstract class CustomAbstractFish extends AbstractFish implements Injectable {
    protected CustomAbstractFish(final EntityType<? extends CustomAbstractFish> entityType,
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
