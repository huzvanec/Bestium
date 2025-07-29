package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Extend this class to implement your own villager with custom behavior or properties.
 * <p>
 * Examples of vanilla {@link AbstractVillager} subclasses: {@link Villager}, {@link WanderingTrader}
 */
public abstract class CustomAbstractVillager extends AbstractVillager implements Injectable {
    /**
     * The constructor of this class.
     *
     * @param entityType the real type of this entity, unlike {@link #getType()}
     * @param level      the level this entity is created in
     */
    protected CustomAbstractVillager(final EntityType<? extends CustomAbstractVillager> entityType,
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
