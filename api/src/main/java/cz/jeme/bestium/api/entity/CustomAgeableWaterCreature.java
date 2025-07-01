package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own aging water creature with custom behavior or properties.
 * <p>
 * Examples of vanilla {@link AgeableWaterCreature} subclasses: {@link Squid}, {@link Dolphin}
 */
@NullMarked
public abstract class CustomAgeableWaterCreature extends AgeableWaterCreature implements Injectable {
    protected CustomAgeableWaterCreature(final EntityType<? extends CustomAgeableWaterCreature> entityType,
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
