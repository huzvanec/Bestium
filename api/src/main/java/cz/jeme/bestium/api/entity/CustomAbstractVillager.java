package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own villager with custom behavior or properties.
 * <p>
 * Examples of vanilla {@link AbstractVillager} subclasses: {@link Villager}, {@link WanderingTrader}
 */
@NullMarked
public abstract class CustomAbstractVillager extends AbstractVillager implements Injectable {
    protected CustomAbstractVillager(final EntityType<? extends CustomAbstractVillager> entityType,
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
