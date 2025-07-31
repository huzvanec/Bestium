package cz.jeme.bestium.api.inject;

import cz.jeme.bestium.api.Bestium;
import cz.jeme.bestium.api.inject.variant.VariantPicker;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Bestium entity that can be injected into the Minecraft runtime.
 * <p>
 * This is an interface because Java does not support multiple class inheritance, and Bestium entities
 * cannot be directly injected into the official vanilla entity hierarchy. While this is technically
 * an interface, most of its methods (marked with {@link ApiStatus.NonExtendable}) are <strong>not intended to be overridden</strong>,
 * they provide essential logic for Bestium entity injection.
 * <p>
 * <strong>This interface may get breaking changes even in minor releases!</strong>
 * <p>
 * Before you start implementing this interface to create your custom abstract class, you should first check the
 * {@code cz.jeme.bestium.api.entity} package. It may already contain a suitable implementation, such as {@code CustomMonster}
 * or {@code CustomAnimal}.
 * If it does not, consider opening an issue or pull request on the <a href="https://github.com/huzvanec/Bestium/issues">official GitHub page</a>
 * to propose the addition.
 * <p>
 * To create an injectable custom entity, you should:
 * <ol>
 *   <li>Create an <strong>abstract</strong> class that extends a vanilla Minecraft entity (e.g. {@code Skeleton}).</li>
 *   <li>Implement {@link Injectable}.</li>
 *   <li>Call {@link #initBestium(EntityType, Level)} in the constructor.</li>
 *   <li>Override {@code getType()} and return {@link #getBestiumBackingType()}.</li>
 *   <li>Override {@code addAdditionalSaveData(ValueOutput)} to call both {@code super.addAdditionalSaveData(ValueOutput)} and {@link #addBestiumAdditionalSaveData(ValueOutput)}.</li>
 * </ol>
 * For example:
 * <pre>{@code
 * public abstract class CustomSkeleton extends Skeleton implements Injectable {
 *     public CustomSkeleton(EntityType<? extends CustomSkeleton> type, Level level) {
 *         super(type, level);
 *         initBestium();
 *     }
 *
 *     @Override
 *     public EntityType<?> getType() {
 *         return getBestiumBackingType();
 *     }
 *
 *     @Override
 *     protected void addAdditionalSaveData(ValueOutput output) {
 *         super.addAdditionalSaveData(output);
 *         addBestiumAdditionalSaveData(output);
 *     }
 * }
 * }</pre>
 * <p>
 * This abstract class can then be extended further to implement specific custom behavior.
 * <p>
 *
 * @see cz.jeme.bestium.api.entity
 */
public interface Injectable {
    /**
     * Returns this object cast as a {@link net.minecraft.world.entity.Entity}.
     * <p>
     * This is safe by contract, as all implementations of this interface must extend {@code Entity}.
     *
     * @return this object cast as an {@code Entity}
     * @throws IllegalStateException if the implementing class does not extend {@code Entity}
     */
    @ApiStatus.NonExtendable
    default @NotNull Entity asBestiumEntity() {
        if (!(this instanceof final Entity entity))
            throw new IllegalStateException("Classes implementing '" + Injectable.class.getName() + "' must extend '" + Entity.class.getName() + "'");
        return entity;
    }

    /**
     * Returns the {@link EntityInjection} object used to inject this entity into the runtime.
     * <p>
     * <strong>Calls to this method are relatively expensive and should be cached if accessed frequently.</strong>
     *
     * @return the registered entity injection
     * @throws IllegalStateException if this entity's {@link EntityInjection} was not yet injected
     */
    @ApiStatus.NonExtendable
    default EntityInjection<?, ?> getBestiumInjection() {
        final EntityInjection<Entity, ?> injection = Bestium.getInjector().getInjections().get(getClass());
        if (injection == null) throw new IllegalStateException("Not injected yet");
        return injection;
    }

    /**
     * Returns the unique {@link Key} associated with this injectable entity.
     * <p>
     * <strong>Calls to this method are relatively expensive and should be cached if accessed frequently.</strong>
     *
     * @return the registered entity key
     * @throws IllegalStateException if this entity's {@link EntityInjection} was not yet injected
     */
    @ApiStatus.NonExtendable
    default Key getBestiumKey() {
        return getBestiumInjection().getKey();
    }

    /**
     * Returns the vanilla {@link EntityType} that backs this custom entity.
     * This is the type used for interaction with vanilla systems such as spawning or serialization.
     * <p>
     * <strong>Calls to this method are relatively expensive and should be cached if accessed frequently.</strong>
     *
     * @return the vanilla backing type
     * @throws IllegalStateException if this entity's {@link EntityInjection} was not yet injected
     * @see EntityInjection#getBackingType()
     * @see #getBestiumRealType()
     */
    @ApiStatus.NonExtendable
    default EntityType<?> getBestiumBackingType() {
        return getBestiumInjection().getBackingType();
    }

    /**
     * Returns the synthetic {@link EntityType} used internally.
     * <p>
     * <strong>Warning:</strong> This type is not safe to send to the client, as it is not recognized
     * by vanilla clients and may cause packet errors or disconnections.
     * <p>
     * <strong>Calls to this method are relatively expensive and should be cached if accessed frequently.</strong>
     *
     * @return the real entity type
     * @throws IllegalStateException if the entity was not properly registered
     * @see #getBestiumBackingType()
     */
    @ApiStatus.NonExtendable
    default EntityType<?> getBestiumRealType() {
        return getBestiumInjection().getRealType();
    }

    /**
     * Writes entity data to persistent storage.
     * Should be called from the overriding implementation of {@code addAdditionalSaveData(ValueOutput)}.
     *
     * @param output the save target to write to
     */
    @ApiStatus.NonExtendable
    default void addBestiumAdditionalSaveData(final ValueOutput output) {
        Bestium.getEntityManager().saveBestiumEntity(this, output);
    }

    /**
     * Initializes the internals this Bestium entity.
     * <p>
     * <strong>This method should be called from the constructor of your entity.</strong>
     * <p>
     * This method sets up variant selection, persistent data, model rendering,
     * and possible data migration.
     *
     * @param entityType the real entity type
     * @param level      the level the entity is spawning or being loaded into
     * @throws IllegalArgumentException if the provided {@code entityType} does not match the injected one
     * @throws IllegalStateException    if the {@link VariantPicker} returns a variant for the wrong entity
     */
    @ApiStatus.NonExtendable
    default void initBestium(final EntityType<?> entityType, final Level level) {
        Bestium.getEntityManager().initializeBestiumEntity(this, entityType, level);
    }
}
