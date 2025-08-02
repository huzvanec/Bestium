package cz.jeme.bestium.api.inject;

import cz.jeme.bestium.api.inject.biome.SpawnRule;
import cz.jeme.bestium.api.inject.variant.BoundEntityVariant;
import cz.jeme.bestium.api.inject.variant.EntityVariant;
import cz.jeme.bestium.api.inject.variant.UnboundEntityVariant;
import cz.jeme.bestium.api.inject.variant.VariantRule;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.phys.Vec3;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents all necessary metadata and configuration for injecting a custom {@link Entity} into the game.
 * <p>
 * Created using {@link #builder(Key, Class, EntityType.EntityFactory, ConvertFunction)}.
 * <p>
 * After an instance is created, it can be injected into the server runtime using {@link EntityInjector#register(Supplier)} )}.
 *
 * @param <M> the type of the Minecraft entity being injected
 * @param <E> the type of the Bukkit entity the Minecraft entity can be converted to
 * @see EntityInjector
 */
public sealed interface EntityInjection<M extends Entity, E extends org.bukkit.entity.Entity> permits EntityInjectionImpl {

    /**
     * Returns the unique key representing this entity type.
     *
     * @return the namespaced key
     */
    Key getKey();

    /**
     * Returns the class of the custom entity.
     *
     * @return the entity class
     */
    Class<M> getEntityClass();

    /**
     * Returns the factory used to create new instances of this entity.
     *
     * @return the entity factory
     */
    EntityType.EntityFactory<M> getEntityFactory();

    /**
     * Returns the function that converts the Minecraft entity of type {@link M} into a Bukkit entity of type {@link E}.
     *
     * @return the convert function
     */
    ConvertFunction<M, E> getConvertFunction();

    /**
     * Returns the backing Minecraft entity type used for client-side rendering and hitbox size.
     *
     * @return the backing type
     */
    EntityType<?> getBackingType();

    /**
     * Returns the category of the entity.
     *
     * @return the mob category
     */
    MobCategory getMobCategory();

    /**
     * Returns the type builder customizer used to modify entity properties.
     *
     * @return the builder customizer
     */
    Consumer<EntityType.Builder<M>> getTypeCustomizer();

    /**
     * Returns the attribute supplier used to define default attributes for this entity.
     * Returns {@code null} if the entity does not extend {@link LivingEntity} and therefore does not have attributes.
     *
     * @return the attribute supplier or {@code null}
     */
    @Nullable
    AttributeSupplier getDefaultAttributes();

    /**
     * Returns the URL of the model associated with this entity.
     *
     * @return the model URL, or {@code null} if none was set
     * @deprecated Use {@link #getVariants()} and {@link EntityVariant#getModelUrl()}.
     */
    @Deprecated(since = "2.1.0")
    @Nullable
    default URL getModelUrl() {
        final Iterator<BoundEntityVariant> it = getVariants().values().iterator();
        return it.hasNext() ? it.next().getModelUrl() : null;
    }

    /**
     * Returns the model name prefix used for entity variants bound to this injection.
     * <p>
     * The returned string is based on the entity's Bestium {@link Key},
     * with the format:
     * <pre>{@code bestium.<key_namespace>.<key_value>}</pre>
     * where slashes in the key's value are replaced with dots.
     * <p>
     * This prefix is used to construct full model names for the entity's variants. For example,
     * if the prefix is
     * <pre>{@code bestium.my_plugin.capybara}</pre>
     * the full variant model name might be:
     * <pre>{@code bestium.my_plugin.capybara.cold}</pre>
     *
     * @return the variant model prefix for this entity
     * @see BoundEntityVariant#getModelName()
     */
    default String getModelPrefix() {
        final Key key = getKey();
        return "bestium." +
               key.namespace() + "." +
               key.value().replace('/', '.');
    }

    /**
     * Returns the model variants associated with this entity injection.
     * <p>
     * The returned map preserves the insertion order of the variants.
     * Keys are the entity variant IDs, and values are the corresponding bound variants.
     *
     * @return an unmodifiable, insertion-ordered map of bound entity variants
     */
    @Unmodifiable
    Map<String, BoundEntityVariant> getVariants();

    /**
     * Returns the model name used to identify this entity's model.
     * <p>
     * The model name is automatically generated based on the entity's key,
     * using the following format:
     * {@code bestium.<namespace>.<value>}.
     * <p>
     * For example, an entity with the key {@code example_plugin:custom_entity}
     * would have the model name {@code bestium.example_plugin.custom_entity}.
     *
     * @return the generated model name, or {@code null} if no model URL was set
     * @see #getKey()
     * @see #getModelUrl()
     */
    @Nullable
    @Deprecated(since = "2.1.0")
    default String getModelName() {
        final Iterator<BoundEntityVariant> it = getVariants().values().iterator();
        return it.hasNext() ? it.next().getModelName() : null;
    }

    /**
     * Returns the {@link VariantRule} responsible for choosing the variant for this
     * entity when in spawns into the world.
     *
     * @return the variant rule for this entity
     */
    VariantRule getVariantRule();

    SpawnRule getSpawnRule();

    /**
     * Returns the synthetic {@link EntityType} created and used internally by Bestium.
     * <p>
     * <strong>Warning:</strong> This type is not safe to send to the client, as it is not recognized
     * by vanilla clients and may cause packet errors or disconnections.
     *
     * @return the real entity type
     * @throws IllegalStateException if this {@link EntityInjection} was not yet injected into the runtime
     * @see #getBackingType()
     */
    EntityType<M> getRealType();

    /**
     * Creates a new {@link Builder} for constructing an {@link EntityInjection}.
     *
     * @param key             a namespaced key such as {@code my_plugin:my_custom_entity}
     * @param entityClass     the class representing your custom entity
     * @param entityFactory   the factory used to instantiate the entity (usually {@code CustomEntityClass::new})
     * @param convertFunction the factory used to create a Bukkit representation of this entity (e.g., {@code CraftAnimals::new})
     * @param <M>             the type of the Minecraft entity being injected
     * @param <E>             the type of the Bukkit entity the Minecraft entity can be converted to
     * @return a new builder instance
     * @see NamespacedKey#NamespacedKey(Plugin, String)
     */
    static <M extends Entity & Injectable, E extends org.bukkit.entity.Entity> Builder<M, E> builder(final Key key,
                                                                                                     final Class<M> entityClass,
                                                                                                     final EntityType.EntityFactory<M> entityFactory,
                                                                                                     final ConvertFunction<M, E> convertFunction) {
        return new EntityInjectionImpl.BuilderImpl<>(
                key,
                entityClass,
                entityFactory,
                convertFunction
        );
    }

    /**
     * A builder used to configure and construct an {@link EntityInjection}.
     *
     * @param <M> the type of the Minecraft entity being injected
     * @param <E> the type of the Bukkit entity the Minecraft entity can be converted to
     */
    sealed interface Builder<M extends Entity, E extends org.bukkit.entity.Entity> extends AbstractBuilder<EntityInjection<M, E>> permits EntityInjectionImpl.BuilderImpl {

        /**
         * Gets the entity key.
         *
         * @return the key
         */
        Key getKey();

        /**
         * Gets the entity class.
         *
         * @return the class
         */
        Class<M> getEntityClass();

        /**
         * Gets the entity factory.
         *
         * @return the factory
         */
        EntityType.EntityFactory<M> getEntityFactory();

        /**
         * Gets the craft entity factory.
         *
         * @return the factory
         */
        ConvertFunction<M, E> getConvertFunction();

        /**
         * Sets the backing type for rendering and size fallback.
         * <p>
         * Defaults to {@link EntityType#SILVERFISH}.
         *
         * @param backingType the fallback entity type
         * @return this builder
         */
        Builder<M, E> setBackingType(final EntityType<?> backingType);

        /**
         * Gets the backing entity type.
         *
         * @return the fallback type
         */
        EntityType<?> getBackingType();

        /**
         * Sets the mob category of the entity.
         * <p>
         * Defaults to {@link MobCategory#MISC}.
         *
         * @param category the mob category
         * @return this builder
         */
        Builder<M, E> setMobCategory(final MobCategory category);

        /**
         * Gets the mob category.
         *
         * @return the category
         */
        MobCategory getMobCategory();

        /**
         * Sets a customizer for the entity type builder.
         * <p>
         * For example, to define passenger attachments use:
         * {@link EntityType.Builder#passengerAttachments(Vec3...)}.
         * <p>
         * Defaults to an empty {@link Consumer}.
         *
         * @param typeBuilder the builder customizer
         * @return this builder
         */
        Builder<M, E> setTypeCustomizer(final Consumer<EntityType.Builder<M>> typeBuilder);

        /**
         * Gets the type builder customizer.
         *
         * @return the builder consumer
         */
        Consumer<EntityType.Builder<M>> getTypeCustomizer();

        /**
         * Sets the default attributes for this entity.
         * Only valid for entities extending {@link LivingEntity}.
         * <p>
         * Defaults to {@link LivingEntity#createLivingAttributes()} if this entity is a {@link LivingEntity}
         * otherwise to {@code null}.
         *
         * @param attributes the attributes
         * @return this builder
         * @throws UnsupportedOperationException if the entity is not a LivingEntity
         * @see AttributeSupplier.Builder
         */
        Builder<M, E> setDefaultAttributes(final AttributeSupplier attributes);

        /**
         * Sets the default attributes for this entity.
         * Only valid for entities extending {@link LivingEntity}.
         * <p>
         * Defaults to {@link LivingEntity#createLivingAttributes()} if this entity is a {@link LivingEntity}
         * otherwise to {@code null}.
         *
         * @param builder the builder used to construct the attributes
         * @return this builder
         * @throws UnsupportedOperationException if the entity is not a LivingEntity
         * @see AttributeSupplier.Builder
         */
        default Builder<M, E> setDefaultAttributes(final AttributeSupplier.Builder builder) {
            return setDefaultAttributes(builder.build());
        }

        /**
         * Gets the attribute supplier, or {@code null} if this entity is not a {@link LivingEntity}.
         *
         * @return the attributes or {@code null}
         */
        @Nullable
        AttributeSupplier getDefaultAttributes();

        /**
         * Sets the model for this entity using a resource bundled in the plugin's JAR.
         * <p>
         * The resource path must be '{@code /}'-separated (e.g., {@code models/my_entity.bbmodel}).
         * <p>
         * This method attempts to load the resource using the bootstrapper's class loader.
         * <p>
         * This overwrites all variants with a single variant with the ID {@code 'default'}.
         *
         * @param bootstrapper the plugin bootstrapper whose class loader will be used to load the resource
         * @param resource     the path to the resource inside the plugin's JAR
         * @return this builder
         * @throws IllegalArgumentException if the resource cannot be found in the JAR file
         * @deprecated Use {@link #addVariant(UnboundEntityVariant)} , {@link #addVariants(Collection)}
         * * and {@link #setVariants(Collection)}.
         */
        @SuppressWarnings("UnstableApiUsage")
        @Deprecated(since = "2.1.0")
        default Builder<M, E> setModel(final PluginBootstrap bootstrapper, final String resource) {
            return setVariants(Set.of(EntityVariant.fromModelResource("default", bootstrapper, resource)));
        }

        /**
         * Sets the model for this entity using a local file on the filesystem.
         * <p>
         * This overwrites all variants with a single variant with the ID {@code 'default'}.
         *
         * @param file the file containing the model
         * @return this builder
         * @throws IllegalArgumentException if the file's URL is invalid
         * @deprecated Use {@link #addVariant(UnboundEntityVariant)} , {@link #addVariants(Collection)}
         * and {@link #setVariants(Collection)}.
         */
        @Deprecated(since = "2.1.0")
        default Builder<M, E> setModel(final File file) {
            return setVariants(Set.of(EntityVariant.fromModelFile("default", file)));
        }

        /**
         * Sets the model for this entity using the given URL.
         * <p>
         * This overwrites all variants with a single variant with the ID {@code 'default'}.
         *
         * @param url the URL pointing to the model file
         * @return this builder
         * @deprecated Use {@link #addVariant(UnboundEntityVariant)} , {@link #addVariants(Collection)}
         * and {@link #setVariants(Collection)}.
         */
        @Deprecated(since = "2.1.0")
        default Builder<M, E> setModel(final URL url) {
            return setVariants(Set.of(EntityVariant.fromModelUrl("default", url)));
        }

        /**
         * Replaces all entity variants providing models for this entity injection with the given collection.
         * <p>
         * This operation discards any previously added variants, whether added via this or other methods.
         * The order of variants in the provided collection matters: the default variant chosen by a spawning entity
         * will be the first variant in this collection.
         * <p>
         * The collection must not contain variants with duplicate IDs. If duplicates exist, they will be
         * silently eliminated with undefined behavior.
         *
         * @param variants the collection of unbound entity variants to set
         * @return this builder
         * @see #addVariant(UnboundEntityVariant)
         * @see #addVariants(UnboundEntityVariant, UnboundEntityVariant, UnboundEntityVariant...)
         * @see #addVariants(Collection)
         * @see #setVariantRule(VariantRule)
         */
        Builder<M, E> setVariants(final Collection<UnboundEntityVariant> variants);

        /**
         * Adds a single entity variant providing a model to this entity injection.
         * <p>
         * If this is the first variant added, it will be used as the default variant when a spawning
         * entity is choosing a model.
         * <p>
         * If a variant with the same ID already exists, this method will ignore the new variant silently.
         *
         * @param variant the unbound entity variant to add
         * @return this builder
         * @see #addVariants(UnboundEntityVariant, UnboundEntityVariant, UnboundEntityVariant...)
         * @see #addVariants(Collection)
         * @see #setVariants(Collection)
         * @see #setVariantRule(VariantRule)
         */
        Builder<M, E> addVariant(final UnboundEntityVariant variant);

        /**
         * Adds a collection of entity variants providing models to this entity injection.
         * <p>
         * The order of variants in the collection matters: if this is the first time variants are added,
         * the first element will be used as the default variant when a spawning entity is choosing a variant.
         * <p>
         * Variants with IDs that duplicate existing variants in the injection will be ignored.
         * If there are duplicate IDs within the provided collection itself, the first variant encountered
         * with that ID will be prioritized.
         *
         * @param variants the collection of unbound entity variants to add
         * @return this builder
         * @see #addVariant(UnboundEntityVariant)
         * @see #addVariants(UnboundEntityVariant, UnboundEntityVariant, UnboundEntityVariant...)
         * @see #setVariants(Collection)
         * @see #setVariantRule(VariantRule)
         */
        default Builder<M, E> addVariants(final Collection<UnboundEntityVariant> variants) {
            variants.forEach(this::addVariant);
            return this;
        }

        /**
         * Adds two or more entity variants providing models to this entity injection.
         * <p>
         * The order of variants matters: if this is the first time variants are added,
         * the first argument will be the default variant selected when a spawning entity is
         * choosing a model.
         * <p>
         * Variants with duplicate IDs compared to already added variants will be ignored.
         * For duplicates within the arguments themselves, the first variant with the ID will be prioritized.
         *
         * @param first  the first unbound entity variant to add
         * @param second the second unbound entity variant to add
         * @param rest   additional unbound entity variants to add
         * @return this builder
         * @see #addVariant(UnboundEntityVariant)
         * @see #addVariants(Collection)
         * @see #setVariants(Collection)
         * @see #setVariantRule(VariantRule)
         */
        default Builder<M, E> addVariants(final UnboundEntityVariant first, final UnboundEntityVariant second, final UnboundEntityVariant... rest) {
            addVariant(first);
            addVariant(second);
            for (final UnboundEntityVariant variant : rest) addVariant(variant);
            return this;
        }

        /**
         * Returns an unmodifiable set of entity variants added to this entity injection.
         * <p>
         * The returned set preserves the order in which variants were added.
         *
         * @return an unmodifiable, ordered set of added entity variants
         */
        @Unmodifiable
        Set<UnboundEntityVariant> getVariants();

        /**
         * Gets the URL of the first registered entity variant.
         *
         * @return the model URL of the first variant, or {@code null} if no variants are registered
         * @deprecated Use {@link #getVariants()} and {@link EntityVariant#getModelUrl()} instead.
         */
        @Deprecated(since = "2.1.0")
        @Nullable
        default URL getModelUrl() {
            Iterator<UnboundEntityVariant> it = getVariants().iterator();
            return it.hasNext() ? it.next().getModelUrl() : null;
        }

        /**
         * Sets the {@link VariantRule} responsible for picking the {@link BoundEntityVariant} when this
         * entity spawns into the world.
         * <p>
         * Defaults to {@link VariantRule#first()}
         *
         * @param variantRule the variant rule
         * @return this builder
         */
        Builder<M, E> setVariantRule(final VariantRule variantRule);

        /**
         * Computes and then sets the {@link VariantRule} responsible for picking the {@link BoundEntityVariant} when
         * this entity spawns into the world.
         * <p>
         * This method may be useful when additional logic needs to run before returning the variant rule.
         * <p>
         * Defaults to {@link VariantRule#first()}
         *
         * @param variantRuleSupplier the supplier providing the variant rule
         * @return this builder
         */
        default Builder<M, E> setComputedVariantRule(final Supplier<VariantRule> variantRuleSupplier) {
            return setVariantRule(variantRuleSupplier.get());
        }

        /**
         * Returns the {@link VariantRule} for this entity.
         *
         * @return the variant rule
         */
        VariantRule getVariantRule();

        Builder<M, E> setSpawnRule(final SpawnRule spawnRule);

        SpawnRule getSpawnRule();

        default Builder<M, E> setComputedSpawnRule(final Supplier<SpawnRule> spawnRuleSupplier) {
            return setSpawnRule(spawnRuleSupplier.get());
        }
    }
}
