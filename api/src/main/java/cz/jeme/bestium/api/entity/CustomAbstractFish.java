package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Extend this class to implement your own fish with custom behavior or properties.
 * <p>
 * This base class does not include schooling behavior like that of cod, salmon, or tropical fish in Minecraft.
 * For schooling behavior, see {@link CustomAbstractSchoolingFish}.
 * <p>
 * Examples of vanilla {@link AbstractFish} subclasses: {@link Tadpole}, {@link Pufferfish}
 */
public abstract class CustomAbstractFish extends AbstractFish implements Injectable {
    /**
     * The constructor of this class.
     *
     * @param entityType the real type of this entity, unlike {@link #getType()}
     * @param level      the level this entity is created in
     */
    protected CustomAbstractFish(final EntityType<? extends CustomAbstractFish> entityType,
                                 final Level level) {
        super(entityType, level);
        bestium_init();
    }

    @Override
    public final EntityType<?> getType() {
        return bestium_getBackingType();
    }

    @Override
    public void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);
        bestium_addAdditionalSaveData(output);
    }

    protected final EntityType<?> realType = bestium_getRealType();

    @Override
    public boolean dismountsUnderwater() {
        return super.dismountsUnderwater();
    }
}
