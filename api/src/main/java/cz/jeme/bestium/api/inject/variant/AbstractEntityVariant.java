package cz.jeme.bestium.api.inject.variant;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jspecify.annotations.Nullable;

import java.net.URL;

sealed abstract class AbstractEntityVariant implements EntityVariant permits BoundEntityVariantImpl, UnboundEntityVariantImpl {
    protected final @KeyPattern.Namespace String id;
    protected final URL modelUrl;

    AbstractEntityVariant(final @KeyPattern.Namespace String id, URL modelUrl) {
        if (!Key.parseableNamespace(id))
            throw new IllegalArgumentException("Entity variant id can only contain [a-z0-9_.-]");
        this.id = id;
        this.modelUrl = modelUrl;
    }

    @Override
    public final @KeyPattern.Namespace String getId() {
        return id;
    }

    @Override
    public final URL getModelUrl() {
        return modelUrl;
    }

    @Override
    public final boolean equals(final @Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractEntityVariant that = (AbstractEntityVariant) o;
        return id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "EntityVariant{" +
               "id='" + id + '\'' +
               ", modelUrl=" + modelUrl +
               '}';
    }
}
