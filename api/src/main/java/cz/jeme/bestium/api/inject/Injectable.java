package cz.jeme.bestium.api.inject;

import cz.jeme.bestium.api.Bestium;
import cz.jeme.bestium.api.util.ModelUtils;
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

/**
 * Represents a Bestium entity that can be injected into the Minecraft runtime.
 * <p>
 * This is an interface because Java does not support multiple class inheritance, and Bestium entities
 * cannot be directly injected into the official vanilla entity hierarchy. While this is technically
 * an interface, its methods are <strong>not</strong> intended to be overridden — they provide essential
 * infrastructure logic for Bestium entity injection.
 * <p>
 * Before you start implementing this interface to create your custom abstract class, you should first check the
 * {@code cz.jeme.bestium.api.entity} package. It may already contain a suitable implementation, such as {@code CustomMonster}
 * or {@code CustomAnimal}.
 * If it does not, consider opening an issue or pull request on the <a href="https://github.com/huzvanec/Bestium/issues">official GitHub page</a>
 * to propose the addition.
 * <p>
 * To create an injectable custom entity, you should:
 * <ol>
 *   <li>Create an abstract class that extends a vanilla Minecraft entity (e.g. {@code Skeleton}).</li>
 *   <li>Implement {@link Injectable}.</li>
 *   <li>Call {@link #bestium_init()} in the constructor.</li>
 *   <li>Override {@code getType()} and return {@link #bestium_backingType()}.</li>
 *   <li>Override {@code addAdditionalSaveData(ValueOutput)} to call both {@code super.addAdditionalSaveData(ValueOutput)} and {@link #bestium_addAdditionalSaveData(ValueOutput)}.</li>
 * </ol>
 * For example:
 * <pre>{@code
 * public abstract class CustomSkeleton extends Skeleton implements Injectable {
 *     public CustomSkeleton(EntityType<? extends CustomSkeleton> type, Level level) {
 *         super(type, level);
 *         bestium_init();
 *     }
 *
 *     @Override
 *     public EntityType<?> getType() {
 *         return bestium_backingType();
 *     }
 *
 *     @Override
 *     protected void addAdditionalSaveData(ValueOutput output) {
 *         super.addAdditionalSaveData(output);
 *         bestium_addAdditionalSaveData(output);
 *     }
 * }
 * }</pre>
 * <p>
 * This abstract class can then be extended further to implement specific custom behavior.
 * <p>
 * <strong>Note:</strong> Calls to {@link #bestium_key()}, {@link #bestium_backingType()}, and {@link #bestium_realType()} are relatively expensive
 * and should be cached if accessed frequently.
 */
@NullMarked
public interface Injectable {
    /**
     * Returns this object cast as a {@link net.minecraft.world.entity.Entity}.
     * <p>
     * This is safe by contract, as all implementations of this interface must extend {@code Entity}.
     *
     * @param <T> the specific type extending both Entity and Injectable
     * @return this object cast as an {@code Entity}
     * @throws IllegalStateException if the implementing class does not extend {@code Entity}
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.NonExtendable
    default <T extends Entity & Injectable> @NotNull T bestium_asEntity() {
        if (!(this instanceof final Entity entity))
            throw new IllegalStateException("Classes implementing '" + Injectable.class.getName() + "' must extend '" + Entity.class.getName() + "'");
        return (T) entity;
    }

    /**
     * Returns the unique {@link Key} associated with this injectable entity.
     *
     * @return the registered entity key
     * @throws NullPointerException if the entity was not properly registered
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    @ApiStatus.NonExtendable
    default Key bestium_key() {
        return Objects.requireNonNull(Bestium.injector().injections().get(bestium_asEntity().getClass()).key());
    }

    /**
     * Returns the vanilla {@link EntityType} that backs this custom entity.
     * This is the type used for interaction with vanilla systems such as spawning or serialization.
     *
     * @return the vanilla backing type
     * @see EntityInjection#backingType()
     */
    @ApiStatus.NonExtendable
    default EntityType<?> bestium_backingType() {
        return Bestium.injector().injections().get(getClass()).backingType();
    }

    /**
     * Returns the synthetic {@link EntityType} used internally.
     * <p>
     * <strong>Warning:</strong> This type is not safe to send to the client, as it is not recognized
     * by vanilla clients and may cause packet errors or disconnections.
     *
     * @return the internal Bestium entity type
     */
    @ApiStatus.NonExtendable
    default EntityType<?> bestium_realType() {
        return Bestium.injector().types().get(getClass());
    }

    /**
     * Writes entity data to persistent storage.
     * Should be called from the overriding implementation of {@code addAdditionalSaveData(ValueOutput)}.
     *
     * @param output the save target to write to
     */
    @ApiStatus.NonExtendable
    default void bestium_addAdditionalSaveData(final ValueOutput output) {
        output.putString(Entity.TAG_ID, bestium_key().asString());
    }

    /**
     * Initializes this Bestium entity.
     * <p>
     * This method sets up persistent tracking data (e.g., for models), and should be called from
     * the constructor of your custom entity class.
     */
    @ApiStatus.NonExtendable
    default void bestium_init() {
        if (Bestium.pluginSupport().betterModel()) {
            bestium_asEntity().getBukkitEntity().getPersistentDataContainer().set(
                    EntityTrackerRegistry.TRACKING_ID,
                    PersistentDataType.STRING,
                    ModelUtils.keyToModelName(bestium_key())
            );
        }
    }
}