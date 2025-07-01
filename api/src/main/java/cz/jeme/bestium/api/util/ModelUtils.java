package cz.jeme.bestium.api.util;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

/**
 * Utility class for handling model-related operations.
 */
@NullMarked
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
