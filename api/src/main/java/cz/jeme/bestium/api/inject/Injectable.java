package cz.jeme.bestium.api.inject;

import cz.jeme.bestium.api.Bestium;
import cz.jeme.bestium.api.util.KeyUtils;
import kr.toxicity.model.api.tracker.EntityTrackerRegistry;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.ValueOutput;
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
        return Objects.requireNonNull(Bestium.injector().injections().get(bestium_asEntity().getClass()).key());
    }

    @ApiStatus.NonExtendable
    default EntityType<?> bestium_backingType() {
        return Bestium.injector().injections().get(getClass()).backingType();
    }

    @ApiStatus.NonExtendable
    default EntityType<?> bestium_realType() {
        return Bestium.injector().types().get(getClass());
    }

    @ApiStatus.NonExtendable
    default void bestium_addAdditionalSaveData(final ValueOutput output) {
        output.putString(Entity.TAG_ID, bestium_key().asString());
    }

    @ApiStatus.NonExtendable
    default void bestium_init() {
        if (Bestium.pluginSupport().betterModel()) {
            bestium_asEntity().getBukkitEntity().getPersistentDataContainer().set(
                    EntityTrackerRegistry.TRACKING_ID,
                    PersistentDataType.STRING,
                    KeyUtils.keyToModelName(bestium_key())
            );
        }
    }
}