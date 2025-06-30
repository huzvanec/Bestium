package cz.jeme.bestium.api.util;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class KeyUtils {
    private KeyUtils() {
        throw new AssertionError();
    }

    public static String keyToModelName(final Key key) {
        return "bestium." + key.namespace() + "." + key.value();
    }
}
