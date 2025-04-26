package cz.jeme.bestium.api.entity;

import cz.jeme.bestium.api.Bestium;
import kr.toxicity.model.api.tracker.Tracker;
import net.kyori.adventure.key.Key;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public interface InjectableEntity {
    @SuppressWarnings("unchecked")
    @ApiStatus.NonExtendable
    default <T extends Entity & InjectableEntity> @NotNull T bestium$asEntity() {
        if (!(this instanceof final Entity entity))
            throw new IllegalStateException("Classes implementing '" + InjectableEntity.class.getName() + "' must extend '" + Entity.class.getName() + "'");
        return (T) entity;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ApiStatus.NonExtendable
    default Key bestium$key() {
        return Objects.requireNonNull(Bestium.instance().injector().injections().get(bestium$asEntity().getClass()).key());
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ApiStatus.NonExtendable
    default EntityType<?> bestium$getType() {
        return Objects.requireNonNull(Bestium.instance().injector().injections().get(bestium$asEntity().getClass()).backingType());
    }

    @ApiStatus.NonExtendable
    default void bestium$addAdditionalSaveData(final CompoundTag compound) {
        compound.putString(Entity.ID_TAG, bestium$key().asString());
    }

    @ApiStatus.NonExtendable
    default void bestium$init() {
        if (
                Bestium.instance().pluginSupport().betterModel() &&
                this instanceof final LivingEntity thisEntity
        ) {
            final Key key = bestium$key();
            thisEntity.getBukkitEntity().getPersistentDataContainer().set(
                    Tracker.TRACKING_ID,
                    PersistentDataType.STRING,
                    key.namespace() + '.' + key.value()
            );
        }
    }
}