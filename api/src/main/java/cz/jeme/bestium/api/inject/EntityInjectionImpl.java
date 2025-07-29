package cz.jeme.bestium.api.inject;

import cz.jeme.bestium.api.inject.variant.BoundEntityVariant;
import cz.jeme.bestium.api.inject.variant.EntityVariant;
import cz.jeme.bestium.api.inject.variant.UnboundEntityVariant;
import net.kyori.adventure.key.Key;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

final class EntityInjectionImpl<T extends Entity, B extends org.bukkit.entity.Entity> implements EntityInjection<T, B> {
    private final Key key;
    private final Class<T> entityClass;
    private final EntityType.EntityFactory<T> entityFactory;
    private final ConvertFunction<T, B> convertFunction;
    private final EntityType<?> backingType;
    private final MobCategory category;
    private final Consumer<EntityType.Builder<T>> typeBuilder;
    private final @Nullable AttributeSupplier attributes;
    private final Map<String, BoundEntityVariant> variants;

    private EntityInjectionImpl(final BuilderImpl<T, B> builder) {
        key = builder.key;
        entityClass = builder.entityClass;
        entityFactory = builder.entityFactory;
        convertFunction = builder.convertFunction;
        backingType = builder.backingType;
        category = builder.category;
        typeBuilder = builder.typeBuilder;
        attributes = builder.attributes;
        variants = builder.variants.stream()
                .map(variant -> variant.bind(this))
                .collect(Collectors.toMap(
                        EntityVariant::getId,
                        Function.identity(),
                        (variant1, variant2) -> {
                            throw new AssertionError(
                                    "Duplicate variant keys encountered in a set. This indicates an issue with the equals function of AbstractEntityVariant."
                            );
                        },
                        LinkedHashMap::new
                ));
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public EntityType.EntityFactory<T> getEntityFactory() {
        return entityFactory;
    }

    @Override
    public ConvertFunction<T, B> getConvertFunction() {
        return convertFunction;
    }

    @Override
    public EntityType<?> getBackingType() {
        return backingType;
    }

    @Override
    public MobCategory getMobCategory() {
        return category;
    }

    @Override
    public Consumer<EntityType.Builder<T>> getTypeCustomizer() {
        return typeBuilder;
    }

    @Override
    public @Nullable AttributeSupplier getDefaultAttributes() {
        return attributes;
    }

    @Override
    public @Unmodifiable Map<String, BoundEntityVariant> getVariants() {
        return Collections.unmodifiableMap(variants);
    }

    static final class BuilderImpl<T extends Entity, B extends org.bukkit.entity.Entity> implements Builder<T, B> {
        private final Key key;
        private final Class<T> entityClass;
        private final EntityType.EntityFactory<T> entityFactory;
        private final ConvertFunction<T, B> convertFunction;
        private final boolean isLivingEntity;

        private EntityType<?> backingType = EntityType.SILVERFISH;
        private MobCategory category = MobCategory.MISC;
        private Consumer<EntityType.Builder<T>> typeBuilder = b -> {
        };
        private @Nullable AttributeSupplier attributes;
        private Set<UnboundEntityVariant> variants = new LinkedHashSet<>();

        public BuilderImpl(final Key key,
                           final Class<T> entityClass,
                           final EntityType.EntityFactory<T> entityFactory,
                           final ConvertFunction<T, B> convertFunction) {
            this.key = key;
            this.entityClass = entityClass;
            this.entityFactory = entityFactory;
            this.convertFunction = convertFunction;

            isLivingEntity = LivingEntity.class.isAssignableFrom(entityClass);
            attributes = isLivingEntity
                    ? LivingEntity.createLivingAttributes().build()
                    : null;
        }


        @Override
        public Key getKey() {
            return key;
        }

        @Override
        public Class<T> getEntityClass() {
            return entityClass;
        }

        @Override
        public EntityType.EntityFactory<T> getEntityFactory() {
            return entityFactory;
        }

        @Override
        public ConvertFunction<T, B> getConvertFunction() {
            return convertFunction;
        }

        @Override
        public Builder<T, B> setBackingType(final EntityType<?> backingType) {
            this.backingType = backingType;
            return this;
        }

        @Override
        public EntityType<?> getBackingType() {
            return backingType;
        }

        @Override
        public Builder<T, B> setMobCategory(final MobCategory category) {
            this.category = category;
            return this;
        }

        @Override
        public MobCategory getMobCategory() {
            return category;
        }

        @Override
        public Builder<T, B> setTypeCustomizer(final Consumer<EntityType.Builder<T>> typeBuilder) {
            this.typeBuilder = typeBuilder;
            return this;
        }

        @Override
        public Consumer<EntityType.Builder<T>> getTypeCustomizer() {
            return typeBuilder;
        }

        @Override
        public Builder<T, B> setDefaultAttributes(final AttributeSupplier attributes) {
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
        public @Nullable AttributeSupplier getDefaultAttributes() {
            return attributes;
        }

        @Override
        public Builder<T, B> addVariant(final UnboundEntityVariant variant) {
            variants.add(variant);
            return this;
        }

        @Override
        public Builder<T, B> setVariants(final Collection<UnboundEntityVariant> variants) {
            this.variants = new LinkedHashSet<>(variants);
            return this;
        }

        @Override
        public @Unmodifiable Set<UnboundEntityVariant> getVariants() {
            return Collections.unmodifiableSet(variants);
        }

        @Override
        public @NotNull EntityInjection<T, B> build() {
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
        if (!(o instanceof final EntityInjectionImpl<?, ?> that)) return false;

        return key.equals(that.key) && entityClass.equals(that.entityClass) && entityFactory.equals(that.entityFactory) && convertFunction.equals(that.convertFunction) && backingType.equals(that.backingType) && category == that.category && typeBuilder.equals(that.typeBuilder) && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + entityClass.hashCode();
        result = 31 * result + entityFactory.hashCode();
        result = 31 * result + convertFunction.hashCode();
        result = 31 * result + backingType.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + typeBuilder.hashCode();
        result = 31 * result + Objects.hashCode(attributes);
        return result;
    }
}
