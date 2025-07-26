package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own ambient creature with custom behavior or properties.
 * <p>
 * Examples of vanilla {@link AmbientCreature} subclasses: {@link Bat}
 */
@NullMarked
public abstract class CustomAmbientCreature extends AmbientCreature implements Injectable {
    protected CustomAmbientCreature(final EntityType<? extends CustomAmbientCreature> entityType,
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
