package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.inject.EntityInjection;
import net.kyori.adventure.key.Key;

/**
 * Represents an {@link EntityVariant} that is bound to an {@link EntityInjection}.
 * <p>
 * A bound variant is created internally when an {@link UnboundEntityVariant} is registered
 * to an injection (e.g., via {@link EntityInjection.Builder#addVariant(UnboundEntityVariant)}).
 * <p>
 * For more information about entity variants, see {@link EntityVariant} and {@link UnboundEntityVariant}.
 */
public sealed interface BoundEntityVariant extends EntityVariant permits BoundEntityVariantImpl {
    /**
     * Returns the {@link EntityInjection} this variant is bound to.
     *
     * @return the injection instance
     */
    EntityInjection<?, ?> getInjection();

    /**
     * Returns the full model name of this bound variant.
     * <p>
     * The model name is a fully-qualified string following the format:
     * <pre>{@code bestium.<entity_key_namespace>.<entity_key_value>.<variant_id>}</pre>
     * For example:
     * <pre>{@code bestium.my_plugin.capybara.warm}</pre>
     * <p>
     * The base of this string is derived from {@link EntityInjection#getModelPrefix()}, which builds the
     * prefix from the entity's {@link Key}.
     *
     * @return the model name string for this variant
     * @see EntityInjection#getModelPrefix()
     */
    String getModelName();
}
