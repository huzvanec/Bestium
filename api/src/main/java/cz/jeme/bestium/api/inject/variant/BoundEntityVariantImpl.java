package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.inject.EntityInjection;

final class BoundEntityVariantImpl extends AbstractEntityVariant implements BoundEntityVariant {
    private final EntityInjection<?, ?> injection;
    private final String modelName;

    BoundEntityVariantImpl(final AbstractEntityVariant variant, final EntityInjection<?, ?> injection) {
        //noinspection PatternValidation
        super(variant.getId(), variant.getModelUrl());
        this.injection = injection;
        modelName = injection.getModelPrefix() + '.' + variant.getId();
    }

    @Override
    public EntityInjection<?, ?> getInjection() {
        return injection;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public String toString() {
        return "BoundEntityVariant{" +
               "id='" + id + '\'' +
               ", modelUrl=" + modelUrl +
               ", injection=" + injection +
               ", modelName='" + modelName + '\'' +
               '}';
    }
}
