package cz.jeme.bestium.api.inject;

import cz.jeme.bestium.api.entity.InjectableEntity;
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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents all necessary metadata and configuration for injecting a custom {@link Entity} into the game.
 * <p>
 * Created using {@link #builder(Key, Class, EntityType.EntityFactory)}.
 * <p>
 * After an instance is created, it can be injected into the server runtime using {@link EntityInjector#register(EntityInjection)}.
 *
 * @param <T> the type of the {@link Entity} being injected
 * @see EntityInjector
 */
@NullMarked
public sealed interface EntityInjection<T extends Entity & InjectableEntity> permits EntityInjectionImpl {

    /**
     * Gets the unique key representing this entity type.
     *
     * @return the namespaced key
     */
    Key key();

    /**
     * Gets the class of the custom entity.
     *
     * @return the entity class
     */
    Class<T> entityClass();

    /**
     * Gets the factory used to create new instances of this entity.
     *
     * @return the entity factory
     */
    EntityType.EntityFactory<T> entityFactory();

    /**
     * Returns the backing Minecraft entity type used for client-side rendering and hitbox size.
     *
     * @return the backing type
     */
    EntityType<?> backingType();

    /**
     * Returns the category of the entity.
     *
     * @return the mob category
     */
    MobCategory mobCategory();

    /**
     * Returns the type builder customizer used to modify entity properties.
     *
     * @return the builder customizer
     */
    Consumer<EntityType.Builder<T>> typeCustomizer();

    /**
     * Returns the attribute supplier used to define default attributes for this entity.
     * Returns {@code null} if the entity does not extend {@link LivingEntity} and therefore does not have attributes.
     *
     * @return the attribute supplier or {@code null}
     */
    @Nullable
    AttributeSupplier attributes();

    /**
     * Returns the URL of the model associated with this entity.
     *
     * @return the model URL, or {@code null} if none was set
     */
    @Nullable
    URL modelUrl();

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
     * @return the generated model name
     * @see #key()
     */
    String modelName();

    /**
     * Creates a new {@link Builder} for constructing an {@link EntityInjection}.
     *
     * @param key           a namespaced key such as {@code my_plugin:my_custom_entity}
     * @param entityClass   the class representing your custom entity
     * @param entityFactory the factory used to instantiate the entity (usually {@code CustomEntityClass::new})
     * @param <T>           the type of the entity
     * @return a new builder instance
     * @see NamespacedKey#NamespacedKey(Plugin, String)
     */
    static <T extends Entity & InjectableEntity> Builder<T> builder(final Key key,
                                                                    final Class<T> entityClass,
                                                                    final EntityType.EntityFactory<T> entityFactory) {
        return new EntityInjectionImpl.BuilderImpl<>(key, entityClass, entityFactory);
    }

    /**
     * A builder used to configure and construct an {@link EntityInjection}.
     *
     * @param <T> the entity type
     */
    sealed interface Builder<T extends Entity & InjectableEntity> extends AbstractBuilder<EntityInjection<T>> permits EntityInjectionImpl.BuilderImpl {

        /**
         * Gets the entity key.
         *
         * @return the key
         */
        Key key();

        /**
         * Gets the entity class.
         *
         * @return the class
         */
        Class<T> entityClass();

        /**
         * Gets the entity factory.
         *
         * @return the factory
         */
        EntityType.EntityFactory<T> entityFactory();

        /**
         * Sets the backing type for rendering and size fallback.
         * <p>
         * Defaults to {@link EntityType#SILVERFISH}.
         *
         * @param backingType the fallback entity type
         * @return this builder
         */
        Builder<T> backingType(final EntityType<?> backingType);

        /**
         * Gets the backing entity type.
         *
         * @return the fallback type
         */
        EntityType<?> backingType();

        /**
         * Sets the mob category of the entity.
         * <p>
         * Defaults to {@link MobCategory#MISC}.
         *
         * @param category the mob category
         * @return this builder
         */
        Builder<T> mobCategory(final MobCategory category);

        /**
         * Gets the mob category.
         *
         * @return the category
         */
        MobCategory mobCategory();

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
        Builder<T> typeCustomizer(final Consumer<EntityType.Builder<T>> typeBuilder);

        /**
         * Gets the type builder customizer.
         *
         * @return the builder consumer
         */
        Consumer<EntityType.Builder<T>> typeCustomizer();

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
        Builder<T> attributes(final AttributeSupplier attributes);

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
        default Builder<T> attributes(final AttributeSupplier.Builder builder) {
            return attributes(builder.build());
        }

        /**
         * Gets the attribute supplier, or {@code null} if this entity is not a {@link LivingEntity}.
         *
         * @return the attributes or {@code null}
         */
        @Nullable
        AttributeSupplier attributes();

        /**
         * Sets the model for this entity using a resource bundled with the given plugin.
         * <p>
         * The resource path must be '{@code /}'-separated (e.g., {@code models/my_entity.bbmodel}).
         *
         * @param plugin   the plugin from which to load the resource
         * @param resource the path to the resource inside the plugin's JAR
         * @return this builder
         * @throws NullPointerException if the resource cannot be found
         */
        default Builder<T> model(final Plugin plugin, final String resource) {
            return model(Objects.requireNonNull(
                    plugin.getClass().getClassLoader().getResource(resource),
                    "Resource not found: '" + resource + "'"
            ));
        }

        /**
         * Sets the model for this entity using a local file on the filesystem.
         *
         * @param file the file containing the model
         * @return this builder
         * @throws IllegalArgumentException if the file's URL is invalid
         */
        default Builder<T> model(final File file) {
            try {
                return model(file.toURI().toURL());
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("Given file has invalid URL: '" + file.getAbsolutePath() + "'", e);
            }
        }

        /**
         * Sets the model for this entity using the given URL.
         *
         * @param url the URL pointing to the model file
         * @return this builder
         */
        Builder<T> model(final URL url);

        /**
         * Gets the URL of the model associated with this entity.
         *
         * @return the model URL, or {@code null} if none was set
         */
        @Nullable
        URL modelUrl();
    }
}
