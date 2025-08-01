package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.inject.EntityInjection;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an {@link EntityVariant} that is not yet associated with any {@link EntityInjection}.
 * <p>
 * Unbound variants are created using the static factory methods in {@link EntityVariant},
 * and later registered to an injection using the {@link EntityInjection.Builder}, e.g.,
 * {@link EntityInjection.Builder#addVariant(UnboundEntityVariant)}.
 * <p>
 * For more details on creating and working with entity variants, see {@link EntityVariant}.
 */
public sealed interface UnboundEntityVariant extends EntityVariant permits UnboundEntityVariantImpl {

    /**
     * Binds this variant to the specified injection and returns a new {@link BoundEntityVariant}.
     * <p>
     * <strong>Note:</strong> This method is intended for internal use only and should not be called directly as a part of this API.
     *
     * @param injection the injection to bind this variant to
     * @return the bound variant
     */
    @ApiStatus.Internal
    BoundEntityVariant bind(final EntityInjection<?, ?> injection);
}