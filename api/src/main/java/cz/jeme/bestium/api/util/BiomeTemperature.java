package cz.jeme.bestium.api.util;

import org.apache.commons.lang3.DoubleRange;

/**
 * Commonly used temperature ranges for Minecraft biomes.
 * <p>
 * These constants provide a loose approximation of Mojang's internal biome temperature distribution.
 * They can be used for rule-based logic, such as determining spawn behavior or applying variants.
 * <p>
 * <strong>Warning:</strong> These temperature bands overlap at boundaries, meaning a
 * temperature value equal to a boundary (e.g., 0.5) will match multiple ranges. If you rely on exclusive
 * classification, you should implement custom logic or adjust the bounds accordingly.
 * <p>
 * <strong>Note:</strong> These are not official categories from Mojang, but they somewhat reflect
 * the general temperature groups used in vanilla Minecraft: cold, temperate, and warm biomes.
 */
public final class BiomeTemperature {
    private BiomeTemperature() {
        throw new AssertionError();
    }

    // DoubleRange does not work correctly when used with Double.MIN_VALUE and Double.MAX_VALUE :|

    /**
     * Cold biome temperatures.
     */
    public static final DoubleRange COLD = DoubleRange.of(-999_999_999, 0.5);

    /**
     * Temperate biome temperatures.
     */
    public static final DoubleRange TEMPERATE = DoubleRange.of(0.5, 0.9);

    /**
     * Warm biome temperatures.
     */
    public static final DoubleRange WARM = DoubleRange.of(0.9, 999_999_999);
}
