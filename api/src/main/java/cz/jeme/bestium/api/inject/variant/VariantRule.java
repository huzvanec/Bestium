package cz.jeme.bestium.api.inject.variant;

import cz.jeme.bestium.api.util.BiomeTemperature;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.apache.commons.lang3.DoubleRange;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * A functional interface with one method used to determine which
 * variant an entity will have after spawning.
 * <p>
 * Before implementing this interface, check the static factory methods,
 * a suitable implementation may already exist.
 * <p>
 * To combine multiple variant rules into one, see {@link #firstMatch(VariantRule...)}.
 */
@FunctionalInterface
public interface VariantRule {
    /**
     * Picks the variant to apply to an entity upon spawning.
     * <p>
     * The returned {@link BoundEntityVariant} must be one of the values in the provided {@code variants} map.
     * If a variant is returned that does not exist in this map, an exception will be thrown.
     * <p>
     * The {@code variants} map may be empty.
     * <p>
     * Only the data exposed by the {@code context} should be used in this method.
     * Accessing other properties of the entity may be unsafe at this stage.
     *
     * @param variants an unmodifiable map of variant IDs to bound variants
     * @param context  a safe snapshot of contextual data from the spawning entity
     * @return the variant to apply, or {@code null} if no variant (model) should be applied
     */
    @Nullable
    BoundEntityVariant apply(
            final @Unmodifiable Map<String, BoundEntityVariant> variants,
            final EntitySpawnContext context
    );

    /**
     * Returns a rule that selects the first variant registered for an entity.
     * If no variants are available, returns {@code null}.
     * <p>
     * This is the default {@link VariantRule} used when no rule is provided
     * in entity injection.
     * <p>
     * <strong>Note:</strong> Unless this entity has no variants, this rule will never return {@code null}. Passing it to {@link #firstMatch(VariantRule...)}
     * will result in all following rules being ignored.
     *
     * @return a rule that always selects the first available variant or {@code null} if none are available
     */
    static VariantRule first() {
        return VariantRules.FIRST;
    }

    /**
     * Returns a rule that randomly selects one of the available variants.
     * If no variants are available, returns {@code null}.
     * <p>
     * <strong>Note:</strong> Unless this entity has no variants, this rule will never return {@code null}. Passing it to {@link #firstMatch(VariantRule...)}
     * will result in all following rules being ignored.
     *
     * @return a rule that selects a random variant
     */
    static VariantRule random() {
        return VariantRules.RANDOM;
    }


    /**
     * Returns a rule that selects a variant based on weighted chances.
     * <p>
     * For example:
     * <pre>{@code
     * VariantRule.weighted(Map.of(
     *     "normal", 10,
     *     "rare", 1
     * ));
     * }</pre>
     * In this case, the {@code "normal"} variant is 10× more likely to be selected than {@code "rare"}.
     * Only the variants explicitly listed in the chance map may be selected. Unlisted variants
     * and variants with weight less than or equal to {@code 0} are silently ignored.
     * <p>
     * <strong>Note:</strong> This rule will never return {@code null}, passing it to {@link #firstMatch(VariantRule...)}
     * will result in all following rules being ignored.
     * <p>
     * <strong>Note:</strong> If the rule attempts to select a non-existent variant ID from the provided map,
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param weights a map of variant IDs to their selection weight (positive integers)
     * @return a chance based rule
     * @throws IllegalArgumentException if the {@code weights} map is empty
     */
    static VariantRule weighted(final Map<String, @Positive Integer> weights) {
        return VariantRules.weighted(weights);
    }

    /**
     * Returns a variant rule that always returns {@code null},
     * no variant (model) will be applied.
     *
     * @return a variant rule always returning {@code null}
     */
    static VariantRule none() {
        return VariantRules.NONE;
    }

    /**
     * Returns a variant rule that always returns the rule with the provided variant ID.
     * <p>
     * <strong>Note:</strong> This rule will never return {@code null}, passing it to {@link #firstMatch(VariantRule...)}
     * will result in all following rules being ignored.
     * <p>
     * <strong>Note:</strong> If the variant ID does not exist and this rule is applied,
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param variantId the ID of the variant to apply
     * @return a variant rule always returning a variant matching the provided ID
     */
    static VariantRule always(final @KeyPattern.Namespace String variantId) {
        return VariantRules.always(variantId);
    }

    /**
     * Returns a rule that returns the provided variant only if the provided biome key matches
     * the one of the entity's spawn location. If the biome does not match,
     * {@code null} is returned.
     * <p>
     * For example:
     * <pre>{@code
     * VariantRule.ifBiome(Key.key("minecraft:plains"), "normal");
     * }</pre>
     * <p>
     * You may omit the {@code "minecraft:"} namespace for vanilla biomes.
     * <p>
     * This rule is designed to be chained using {@link #firstMatch(VariantRule...)}.
     * <p>
     * <strong>Note:</strong> If the variant ID does not exist and this rule is applied,
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param biomeKey  the biome key to match
     * @param variantId the ID of the variant to apply
     * @return a biome based rule
     * @see #ifBiomes(Set, String)
     */
    static VariantRule ifBiome(final Key biomeKey, final @KeyPattern.Namespace String variantId) {
        return ifBiomes(Set.of(biomeKey), variantId);
    }

    /**
     * Returns a rule that returns the provided variant only if one of the provided biome
     * keys matches the one of the entity's spawn location. If the biome does not match,
     * {@code null} is returned.
     * <p>
     * For example:
     * <pre>{@code
     * VariantRule.ifBiomes(Set.of(
     *    Key.key("minecraft:plains"),
     *    Key.key("minecraft:forest")
     * ), "normal");
     * }</pre>
     * <p>
     * You may omit the {@code "minecraft:"} namespace for vanilla biomes.
     * <p>
     * This rule is designed to be chained using {@link #firstMatch(VariantRule...)}.
     * <p>
     * <strong>Note:</strong> If the variant ID does not exist and this rule is applied,
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param biomeKeys the biome keys to match
     * @param variantId the ID of the variant to apply
     * @return a biome based rule
     * @see #ifBiome(Key, String)
     */
    static VariantRule ifBiomes(final Set<Key> biomeKeys, final @KeyPattern.Namespace String variantId) {
        return VariantRules.ifBiomes(biomeKeys, variantId);
    }

    /**
     * Returns a rule that returns the provided variant only if the provided world key matches
     * the one of the entity's spawn location. If the world does not match,
     * {@code null} is returned.
     * <p>
     * For example:
     * <pre>{@code
     * VariantRule.ifWorld(Key.key("minecraft:overworld"), "normal");
     * }</pre>
     * <p>
     * You may omit the {@code "minecraft:"} namespace for vanilla worlds.
     * <p>
     * This rule is designed to be chained using {@link #firstMatch(VariantRule...)}.
     * <p>
     * <strong>Note:</strong> If the variant ID does not exist and this rule is applied,
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param worldKey  the world key to match
     * @param variantId the ID of the variant to apply
     * @return a world based rule
     * @see #ifWorlds(Set, String)
     */
    static VariantRule ifWorld(final Key worldKey, final @KeyPattern.Namespace String variantId) {
        return ifWorlds(Set.of(worldKey), variantId);
    }

    /**
     * Returns a rule that returns the provided variant only if one of the provided world
     * keys matches the one of the entity's spawn location. If the world does not match,
     * {@code null} is returned.
     * <p>
     * For example:
     * <pre>{@code
     * VariantRule.ifWorlds(Set.of(
     *    Key.key("minecraft:the_end"),
     *    Key.key("minecraft:the_nether")
     * ), "normal");
     * }</pre>
     * <p>
     * You may omit the {@code "minecraft:"} namespace for vanilla worlds.
     * <p>
     * This rule is designed to be chained using {@link #firstMatch(VariantRule...)}.
     * <p>
     * <strong>Note:</strong> If the variant ID does not exist and this rule is applied,
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param worldKeys the world keys to match
     * @param variantId the ID of the variant to apply
     * @return a world based rule
     * @see #ifWorld(Key, String)
     */
    static VariantRule ifWorlds(final Set<Key> worldKeys, final @KeyPattern.Namespace String variantId) {
        return VariantRules.ifWorlds(worldKeys, variantId);
    }

    /**
     * Returns a rule that returns the provided variant only if the temperature of the entity's spawn location
     * is in the provided temperature range. If not, {@code null} is returned.
     * <p>
     * For example:
     * <pre>{@code
     * VariantRule.ifTemperature(DoubleRange.of(Double.MIN_VALUE, 0.5), "cold");
     * }</pre>
     * <p>
     * You may also use the predefined ranges in {@link BiomeTemperature}.
     * <p>
     * This rule is designed to be chained using {@link #firstMatch(VariantRule...)}.
     * <p>
     * <strong>Note:</strong> If the variant ID does not exist and this rule is applied,
     * an {@link IllegalArgumentException} is thrown.
     *
     * @param temperatureRange the temperature range when the variant is applied
     * @param variantId        the ID of the variant to apply
     * @return a temperature based rule
     * @see BiomeTemperature#COLD
     * @see BiomeTemperature#TEMPERATE
     * @see BiomeTemperature#WARM
     */
    static VariantRule ifTemperature(final DoubleRange temperatureRange, final @KeyPattern.Namespace String variantId) {
        return VariantRules.ifTemperature(temperatureRange, variantId);
    }

    /**
     * Combines multiple variant rules into a single rule by evaluating them in order, from first to last.
     * <p>
     * The first rule that returns a non-null {@link BoundEntityVariant} will be used. All subsequent
     * rules are ignored. This creates layered fallback logic where more specific rules are prioritized.
     * <p>
     * Variant rules can be categorized into two types:
     * <ul>
     *     <li>Nullable rules (allow fallthrough, e.g,{@link #ifBiome(Key, String)}, {@link #ifWorld(Key, String)}, {@link #ifTemperature(DoubleRange, String)})</li>
     *     <li>Non-null rules (always return a rule and stop evaluation, e.g., {@link #always(String)}, {@link #random()}, {@link #weighted(Map)})</li>
     * </ul>
     * <strong>Important:</strong> A rule that never returns {@code null} (like {@link #always(String)})
     * should <em>only</em> be used as the final rule in the chain, acting as a fallback.
     * Using a non-null rule earlier in the sequence will prevent any following rules from being evaluated.
     * <p>
     * Example (complex) usage:
     * <pre>{@code
     * VariantRule.firstMatch(
     *    // complicated custom rule:
     *    // if the entity is named BOB, apply "bob" variant
     *    (variants, ctx) -> {
     *       Component name = ctx.getEntity().customName();
     *       if (name == null) return null;
     *       String nameStr = PlainTextComponentSerializer.plainText().serialize(name);
     *       return nameStr.equalsIgnoreCase("bob") ? variants.get("bob") : null;
     *    },
     *    // apply "desert" variant in deserts
     *    VariantRule.ifBiome(Key.key("minecraft:desert"), "desert"),
     *    // apply "jungle" variant in jungles
     *    VariantRule.ifBiomes(Set.of(
     *       Key.key("minecraft:jungle"),
     *       Key.key("minecraft:bamboo_jungle"),
     *       Key.key("minecraft:sparse_jungle"),
     *    ), "jungle"),
     *    // apply "warm" variant warm biomes other than desert and jungle
     *    VariantRule.ifTemperature(BiomeTemperature.WARM, "warm"),
     *    // apply "cold" variant in cold biomes
     *    VariantRule.ifTemperature(BiomeTemperature.COLD, "cold"),
     *    // fallback: apply "normal" variant in all other biomes
     *    VariantRule.always("normal")
     * )
     * }</pre>
     *
     * @param rules rules the variant rules to evaluate in order
     * @return a {@link VariantRule} returning the first non-null variant
     */
    static VariantRule firstMatch(final VariantRule... rules) {
        return VariantRules.firstMatch(rules);
    }
}
