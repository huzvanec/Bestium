package cz.jeme.bestium.api.inject;

import cz.jeme.bestium.api.Bestium;
import cz.jeme.bestium.api.inject.variant.BoundEntityVariant;
import cz.jeme.bestium.api.inject.variant.VariantPicker;
import kr.toxicity.model.api.tracker.EntityTrackerRegistry;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

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
     * @param <T> the specific type extending both Entity and Injectable
     * @return this object cast as an {@code Entity}
     * @throws IllegalStateException if the implementing class does not extend {@code Entity}
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.NonExtendable
    default <T extends Entity> @NotNull T asBestiumEntity() {
        if (!(this instanceof final Entity entity))
            throw new IllegalStateException("Classes implementing '" + Injectable.class.getName() + "' must extend '" + Entity.class.getName() + "'");
        return (T) entity;
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
        output.putString(Entity.TAG_ID, getBestiumKey().asString());
    }

    /**
     * Holder class for lazily initializing and caching Bestium persistent data keys.
     * <p>
     * This ensures that the keys are created only when first accessed,
     * preventing early initialization issues before Bestium is fully loaded.
     */
    @ApiStatus.Internal
    final class KeyHolder {
        private KeyHolder() {
            throw new AssertionError();
        }

        private static @Nullable NamespacedKey BESTIUM_ID_KEY;
        private static @Nullable NamespacedKey BESTIUM_VARIANT_KEY;

        /**
         * Returns the persistent data key used to identify injected entities.
         * <p>
         * This key is stored in an entity's {@link PersistentDataContainer}
         * and holds a {@link PersistentDataType#STRING} representing the injected entity's {@link Key}.
         * <p>
         * Presence of this key signifies that the entity was spawned via the Bestium injection system.
         *
         * @return the namespaced key
         * @see Bestium#isInjectedEntity(org.bukkit.entity.Entity)
         * @see Bestium#getInjectedEntityKey(org.bukkit.entity.Entity)
         * @see Bestium#requireInjectedEntityKey(org.bukkit.entity.Entity)
         */
        public static NamespacedKey getBestiumIdKey() {
            if (BESTIUM_ID_KEY == null)
                BESTIUM_ID_KEY = Bestium.getBestium().createKey("bestium_id");
            return BESTIUM_ID_KEY;
        }

        /**
         * Returns the persistent data key used to identify Bestium entity variants.
         * <p>
         * This key is stored in an entity's {@link PersistentDataContainer}
         * and holds a {@link PersistentDataType#STRING} representing the injected entity's variant ID.
         * <p>
         * If the value is {@code "NO_MODEL"}, it signals the entity should not have any model.
         *
         * @return the namespaced key
         */
        public static NamespacedKey getBestiumVariantKey() {
            if (BESTIUM_VARIANT_KEY == null)
                BESTIUM_VARIANT_KEY = Bestium.getBestium().createKey("bestium_variant");
            return BESTIUM_VARIANT_KEY;
        }
    }

    /**
     * Initializes this Bestium entity.
     * <p>
     * This method should be called from the constructor of your custom entity base class.
     *
     * @param entityType the real entity type of this entity
     * @param level      the level this entity is created in
     * @throws IllegalArgumentException if the entity type is not the real entity type of this entity
     * @throws IllegalStateException    when the {@link VariantPicker} of this entity's {@link EntityInjection} returns
     *                                  a variant that does not belong to this entity
     */
    @ApiStatus.NonExtendable
    default void initBestium(final EntityType<?> entityType, final Level level) {
        final EntityInjection<?, ?> injection = getBestiumInjection();

        if (entityType != injection.getRealType()) throw new IllegalArgumentException(
                "Provided entity type is not the real entity type: '" + entityType + "'"
        );

        final Key key = injection.getKey();
        final Entity entity = asBestiumEntity(); // this injectable as a NMS entity
        final var bukkitEntity = entity.getBukkitEntity(); // as Bukkit to use persistent data api
        final PersistentDataContainer container = bukkitEntity.getPersistentDataContainer();

        // save the Bestium id
        container.set(
                KeyHolder.getBestiumIdKey(),
                PersistentDataType.STRING,
                key.asString()
        );

        // read existing entity variant
        final String existingVariant = container.get(
                KeyHolder.getBestiumVariantKey(),
                PersistentDataType.STRING
        );

        // if the entity already has a variant (even NULL), return
        // that means the entity was already spawned in and this is (very probably)
        // just a server restart and we want to persist entity variants over restarts
        // otherwise animals could change variants even with the simplest random-picking
        // variant implementation
        if (existingVariant != null) return;
        // the mob does not have a variant yet, pick it and store it

        // pick a variant
        final VariantPicker.Context pickerCtx = new VariantPicker.Context(
                entity,
                getBestiumRealType(),
                injection
        );
        final BoundEntityVariant variant = injection.getVariantPicker().pick(injection.getVariants(), pickerCtx);
        if (variant == null) {
            // null was returned, this means that the entity should not have a model (variant)
            container.remove(EntityTrackerRegistry.TRACKING_ID); // remove any model if present
            // store Bestium variant as NO_MODEL
            // this should be somewhat ok, because variant IDs check for Key namespace regex, which is
            // [a-z0-9.-_], meaning no upper case allowed
            container.set(
                    KeyHolder.getBestiumVariantKey(),
                    PersistentDataType.STRING,
                    "NO_MODEL"
            );
        } else { // a variant was returned for this entity
            if (!variant.getInjection().getKey().equals(key))
                // the variant is from a different entity
                throw new IllegalStateException(
                        "Provided entity variant is registered for a different entity: " +
                        variant.getInjection().getKey().asString() +
                        " instead of " +
                        key.asString()
                );
            // store the variant id
            container.set(
                    KeyHolder.getBestiumVariantKey(),
                    PersistentDataType.STRING,
                    variant.getId()
            );
            // check if better model is loaded, then render model
            if (Bestium.getPluginSupport().betterModel()) {
                // normal BetterModel API for adding models to entities cannot be used here
                // as this is run even before the entity is loaded, BetterModel freaks out
                // and detaches the model from the entity
                container.set(
                        EntityTrackerRegistry.TRACKING_ID,
                        PersistentDataType.STRING,
                        variant.getModelName()
                );
            }
        }
    }
}
