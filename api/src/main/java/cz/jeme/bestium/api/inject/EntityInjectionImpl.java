package cz.jeme.bestium.api.inject;

import cz.jeme.bestium.api.entity.Injectable;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;

@NullMarked
final class EntityInjectionImpl<T extends Entity & Injectable> implements EntityInjection<T> {
    private final Key key;
    private final Class<T> entityClass;
    private final EntityType.EntityFactory<T> entityFactory;
    private final EntityType<?> backingType;
    private final MobCategory category;
    private final Consumer<EntityType.Builder<T>> typeBuilder;
    private final @Nullable String modelName;
    private final @Nullable AttributeSupplier attributes;
    private final @Nullable URL modelUrl;

    private EntityInjectionImpl(final BuilderImpl<T> builder) {
        key = builder.key;
        entityClass = builder.entityClass;
        entityFactory = builder.entityFactory;
        backingType = builder.backingType;
        category = builder.category;
        typeBuilder = builder.typeBuilder;
        attributes = builder.attributes;
        modelUrl = builder.modelUrl;
        modelName = modelUrl == null
                ? null
                : "bestium" + '.' + key.namespace() + '.' + key.value();
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public Class<T> entityClass() {
        return entityClass;
    }

    @Override
    public EntityType.EntityFactory<T> entityFactory() {
        return entityFactory;
    }

    @Override
    public EntityType<?> backingType() {
        return backingType;
    }

    @Override
    public MobCategory mobCategory() {
        return category;
    }

    @Override
    public Consumer<EntityType.Builder<T>> typeCustomizer() {
        return typeBuilder;
    }

    @Override
    public @Nullable AttributeSupplier attributes() {
        return attributes;
    }

    @Override
    public @Nullable URL modelUrl() {
        return modelUrl;
    }

    @Override
    public @Nullable String modelName() {
        return modelName;
    }

    static final class BuilderImpl<T extends Entity & Injectable> implements Builder<T> {
        private final Key key;
        private final Class<T> entityClass;
        private final EntityType.EntityFactory<T> entityFactory;
        private final boolean isLivingEntity;

        private EntityType<?> backingType = EntityType.SILVERFISH;
        private MobCategory category = MobCategory.MISC;
        private Consumer<EntityType.Builder<T>> typeBuilder = b -> {
        };
        private @Nullable AttributeSupplier attributes;
        private @Nullable URL modelUrl;

        public BuilderImpl(final Key key,
                           final Class<T> entityClass,
                           final EntityType.EntityFactory<T> entityFactory) {
            this.key = key;
            this.entityClass = entityClass;
            this.entityFactory = entityFactory;

            isLivingEntity = LivingEntity.class.isAssignableFrom(entityClass);
            attributes = isLivingEntity
                    ? LivingEntity.createLivingAttributes().build()
                    : null;
        }


        @Override
        public Key key() {
            return key;
        }

        @Override
        public Class<T> entityClass() {
            return entityClass;
        }

        @Override
        public EntityType.EntityFactory<T> entityFactory() {
            return entityFactory;
        }

        @Override
        public Builder<T> backingType(final EntityType<?> backingType) {
            this.backingType = backingType;
            return this;
        }

        @Override
        public EntityType<?> backingType() {
            return backingType;
        }

        @Override
        public Builder<T> mobCategory(final MobCategory category) {
            this.category = category;
            return this;
        }

        @Override
        public MobCategory mobCategory() {
            return category;
        }

        @Override
        public Builder<T> typeCustomizer(final Consumer<EntityType.Builder<T>> typeBuilder) {
            this.typeBuilder = typeBuilder;
            return this;
        }

        @Override
        public Consumer<EntityType.Builder<T>> typeCustomizer() {
            return typeBuilder;
        }

        @Override
        public Builder<T> attributes(final AttributeSupplier attributes) {
            if (!isLivingEntity)
                throw new UnsupportedOperationException(
                        "'" + entityClass.getName() +
                        "' does not extend '" +
                        LivingEntity.class.getName() +
                        "'. Only living entities can have attributes."
                );
            this.attributes = attributes;
            return this;
        }

        @Override
        public @Nullable AttributeSupplier attributes() {
            return attributes;
        }

        @Override
        public Builder<T> model(final URL url) {
            this.modelUrl = url;
            return this;
        }

        @Override
        public @Nullable URL modelUrl() {
            return modelUrl;
        }

        @Override
        public @NotNull EntityInjection<T> build() {
            return new EntityInjectionImpl<>(this);
        }
    }

    @Override
    public String toString() {
        return "EntityInjectionImpl{" +
               "key=" + key +
               ", entityClass=" + entityClass.getName() +
               ", backingType=" + backingType +
               ", category=" + category +
               '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (!(o instanceof final EntityInjectionImpl<?> that)) return false;

        return key.equals(that.key) && entityClass.equals(that.entityClass) && entityFactory.equals(that.entityFactory) && backingType.equals(that.backingType) && category == that.category && typeBuilder.equals(that.typeBuilder) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + entityClass.hashCode();
        result = 31 * result + entityFactory.hashCode();
        result = 31 * result + backingType.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + typeBuilder.hashCode();
        result = 31 * result + Objects.hashCode(attributes);
        return result;
    }
}
