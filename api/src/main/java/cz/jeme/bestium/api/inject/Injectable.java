package cz.jeme.bestium.api.inject;

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
public interface Injectable {
    @SuppressWarnings("unchecked")
    @ApiStatus.NonExtendable
    default <T extends Entity & Injectable> @NotNull T bestium_asEntity() {
        if (!(this instanceof final Entity entity))
            throw new IllegalStateException("Classes implementing '" + Injectable.class.getName() + "' must extend '" + Entity.class.getName() + "'");
        return (T) entity;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ApiStatus.NonExtendable
    default Key bestium_key() {
        return Objects.requireNonNull(Bestium.instance().injector().injections().get(bestium_asEntity().getClass()).key());
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ApiStatus.NonExtendable
    default EntityType<?> bestium_getType() {
        return Objects.requireNonNull(Bestium.instance().injector().injections().get(bestium_asEntity().getClass()).backingType());
    }

    @ApiStatus.NonExtendable
    default void bestium_addAdditionalSaveData(final CompoundTag compound) {
        compound.putString(Entity.ID_TAG, bestium_key().asString());
    }

    @ApiStatus.NonExtendable
    default void bestium_init() {
        if (
                Bestium.instance().pluginSupport().betterModel() &&
                this instanceof final LivingEntity thisEntity
        ) {
            final Key key = bestium_key();
            thisEntity.getBukkitEntity().getPersistentDataContainer().set(
                    Tracker.TRACKING_ID,
                    PersistentDataType.STRING,
                    key.namespace() + '.' + key.value()
            );
        }
    }
}