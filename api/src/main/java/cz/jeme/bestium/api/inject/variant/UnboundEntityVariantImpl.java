package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.inject.EntityInjection;
import net.kyori.adventure.key.KeyPattern;

import java.net.URL;

final class UnboundEntityVariantImpl extends AbstractEntityVariant implements UnboundEntityVariant {
    UnboundEntityVariantImpl(final @KeyPattern.Namespace String id, final URL modelUrl) {
        super(id, modelUrl);
    }

    @Override
    public BoundEntityVariant bind(final EntityInjection<?, ?> injection) {
        return new BoundEntityVariantImpl(this, injection);
    }
}
