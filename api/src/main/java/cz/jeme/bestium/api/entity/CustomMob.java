package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.inject.Injectable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NullMarked;

/**
 * Extend this class to implement your own mob with custom behavior or properties.
 * <p>
 * Before directly extending this class, you should consider extending {@link CustomPathfinderMob} or {@link CustomAmbientCreature}.
 * <p>
 * Examples of vanilla {@link CustomMob} subclasses: {@link Slime}, {@link Ghast}, {@link Phantom}
 */
@NullMarked
public abstract class CustomMob extends Mob implements Injectable {
    protected CustomMob(final EntityType<? extends CustomMob> entityType,
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
