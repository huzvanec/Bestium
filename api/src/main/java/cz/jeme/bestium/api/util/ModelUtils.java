package cz.jeme.bestium.api.util;

import cz.jeme.bestium.api.inject.EntityInjection;
import cz.jeme.bestium.api.inject.variant.BoundEntityVariant;
import net.kyori.adventure.key.Key;

/**
 * Utility class for handling model-related operations.
 *
 * @deprecated Replaced by variants API, see:
 * <ul>
 *     <li>package {@link cz.jeme.bestium.api.inject.variant}</li>
 *     <li>{@link EntityInjection#getModelPrefix()}</li>
 *     <li>{@link BoundEntityVariant#getModelName()}</li>
 * </ul>
 */
@Deprecated(since = "2.1.0")
public final class ModelUtils {
    private ModelUtils() {
        throw new AssertionError();
    }

    /**
     * Converts a {@link Key} to a standardized model name string in the format:
     * {@code bestium.<namespace>.<value>}.
     *
     * @param key the key to convert
     * @return the corresponding model name string
     */
    public static String keyToModelName(final Key key) {
        return "bestium." + key.namespace() + "." + key.value();
    }
}
