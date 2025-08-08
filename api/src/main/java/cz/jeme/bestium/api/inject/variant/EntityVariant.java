package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.inject.EntityInjection;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import net.kyori.adventure.key.KeyPattern;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Represents a Bestium entity variant.
 * <p>
 * A variant defines a specific model for an entity, identified by a per-entity-unique variant ID
 * (matching {@code [a-z0-9_.-]}). Each entity may have multiple variants registered through an
 * {@link EntityInjection}.
 * <p>
 * Variants are first created as unbound using one of the static factory methods:
 * <ul>
 *     <li>{@link #fromModelUrl(String, URL)}</li>
 *     <li>{@link #fromModelFile(String, File)}</li>
 *     <li>{@link #fromModelResource(String, PluginBootstrap, String)}</li>
 * </ul>
 * Once created and added to an {@link EntityInjection} (e.g., via {@link EntityInjection.Builder#addVariant(UnboundEntityVariant)}),
 * the variant is automatically bound to that injection and becomes a {@link BoundEntityVariant}.
 */
@SuppressWarnings("UnstableApiUsage")
public sealed interface EntityVariant permits AbstractEntityVariant, UnboundEntityVariant, BoundEntityVariant {
    /**
     * Constructs an unbound entity variant from a variant ID and a model URL.
     *
     * @param id       the ID of this variant, e.g., {@code "warm"} or {@code "cold"}
     * @param modelUrl the URL of the model to apply when an entity uses this variant
     * @return the unbound entity variant
     */
    static UnboundEntityVariant fromModelUrl(final @KeyPattern.Namespace String id, final URL modelUrl) {
        return new UnboundEntityVariantImpl(id, modelUrl);
    }

    /**
     * Constructs an unbound entity variant from a variant ID and a model file on the local filesystem.
     *
     * @param id        the ID of this variant, e.g., {@code "warm"} or {@code "cold"}
     * @param modelFile the model file to apply when an entity uses this variant
     * @return the unbound entity variant
     * @throws IllegalArgumentException if the file cannot be converted to a valid URL
     */
    static UnboundEntityVariant fromModelFile(final @KeyPattern.Namespace String id, final File modelFile) {
        try {
            return fromModelUrl(id, modelFile.toURI().toURL());
        } catch (final MalformedURLException e) {
            throw new IllegalArgumentException(
                    "Given model file has invalid URL: '"
                    + modelFile.getAbsolutePath() + "'",
                    e
            );
        }
    }

    /**
     * Constructs an unbound entity variant from a variant ID and a resource file bundled with a plugin.
     *
     * @param id           the ID of this variant, e.g., {@code "warm"} or {@code "cold"}
     * @param bootstrapper the plugin bootstrapper whose class loader is used to locate the resource
     * @param resourceName the name of the resource (e.g., {@code "models/capybara.bbmodel"})
     * @return the unbound entity variant
     * @throws IllegalArgumentException if the resource is not found
     */
    static UnboundEntityVariant fromModelResource(final @KeyPattern.Namespace String id,
                                                  final PluginBootstrap bootstrapper,
                                                  final String resourceName) {
        return fromModelResource(id, bootstrapper.getClass(), resourceName);
    }

    /**
     * Constructs an unbound entity variant from a variant ID and a resource file bundled with a plugin.
     *
     * @param id           the ID of this variant, e.g., {@code "warm"} or {@code "cold"}
     * @param clazz        a class from your plugin whose class loader is used to locate the resource
     * @param resourceName the name of the resource (e.g., {@code "models/capybara.bbmodel"})
     * @return the unbound entity variant
     * @throws IllegalArgumentException if the resource is not found or
     *                                  if the class loader is not a paper plugin class loader
     */
    static UnboundEntityVariant fromModelResource(final @KeyPattern.Namespace String id,
                                                  final Class<?> clazz,
                                                  final String resourceName) {
        final ClassLoader classLoader = clazz.getClassLoader();
        if (!(classLoader instanceof ConfiguredPluginClassLoader)) throw new IllegalArgumentException(
                "The provided class was not loaded by a paper plugin class loader: '" + clazz.getName() + "'"
        );
        final URL url = classLoader.getResource(resourceName);
        if (url == null) throw new IllegalArgumentException(
                "Entity model resource not found: '" + resourceName + "'"
        );
        return fromModelUrl(id, url);
    }

    /**
     * Returns the ID of this entity variant.
     *
     * @return the variant ID
     */
    @KeyPattern.Namespace
    String getId();

    /**
     * Returns the URL of the model associated with this variant.
     *
     * @return the model URL
     */
    URL getModelUrl();
}
