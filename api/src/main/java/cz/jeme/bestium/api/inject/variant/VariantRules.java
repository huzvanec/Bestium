package cz.jeme.bestium.api.inject.variant;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.apache.commons.lang3.DoubleRange;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A collection of useful variant rules.
 * <p>
 * Accessible via static methods in the {@link VariantRule} interface.
 */
final class VariantRules {
    private VariantRules() {
        throw new AssertionError();
    }

    private static IllegalArgumentException unknownVariant(
            Map<String, BoundEntityVariant> variants,
            final EntitySpawnContext ctx,
            final String... unknownIds
    ) {
        final boolean single = unknownIds.length == 1;
        return new IllegalArgumentException(
                "Unknown variant" + (single ? "" : "s") + ": " + (single ? "'" : "[") +
                String.join(", ", unknownIds) + (single ? "'" : "]") +
                " for entity '" + ctx.getInjection().getRealType() + "'. Known variants: [" +
                String.join(", ", variants.keySet()) + "]"
        );
    }

    public static final VariantRule NONE = (variants, ctx) -> null;

    public static final VariantRule FIRST = (variants, ctx) -> {
        final Iterator<BoundEntityVariant> it = variants.values().iterator();
        return it.hasNext() ? it.next() : null;
    };

    public static final VariantRule RANDOM = (variants, ctx) -> {
        if (variants.isEmpty()) return null;
        if (variants.size() == 1) return variants.values().iterator().next();
        final int randomIndex = ThreadLocalRandom.current().nextInt(variants.size());
        int i = 0;
        for (final BoundEntityVariant variant : variants.values()) {
            if (i == randomIndex) return variant;
            i++;
        }
        throw new AssertionError();
    };

    public static VariantRule always(final @KeyPattern.Namespace String variantId) {
        return (variants, ctx) -> {
            final BoundEntityVariant variant = variants.get(variantId);
            if (variant == null) throw unknownVariant(variants, ctx, variantId);
            return variant;
        };
    }

    public static VariantRule weighted(final Map<String, Integer> weights) {
        if (weights.isEmpty()) throw new IllegalArgumentException("Weights map must not be empty");

        final List<String> weightedList = new ArrayList<>();
        for (final Map.Entry<String, Integer> entry : weights.entrySet()) {
            final String variantId = entry.getKey();
            final int value = entry.getValue();
            if (value <= 0) continue;

            for (int i = 0; i < value; i++) {
                weightedList.add(variantId);
            }
        }
        final int size = weightedList.size();
        return (variants, ctx) -> {
            if (variants.isEmpty()) {
                // if the entity has no variants, that means all the variant
                // IDs in the weights map are unknown
                throw unknownVariant(variants, ctx, weights.keySet().toArray(String[]::new));
            }
            final int randomIndex = ThreadLocalRandom.current().nextInt(size);
            final String variantId = weightedList.get(randomIndex);
            final BoundEntityVariant variant = variants.get(variantId);
            if (variant == null) throw unknownVariant(variants, ctx, variantId);
            return variant;
        };
    }

    public static VariantRule ifBiomes(
            final Set<Key> biomeKeys,
            final @KeyPattern.Namespace String variantId
    ) {
        return (variants, ctx) -> {
            if (!variants.containsKey(variantId)) throw unknownVariant(variants, ctx, variantId);
            final Entity entity = ctx.getEntity();
            final NamespacedKey currentBiomeKey = entity.getWorld().getBiome(entity.getLocation()).getKey();
            return (biomeKeys.contains(currentBiomeKey)) ? variants.get(variantId) : null;
        };
    }


    public static VariantRule ifWorlds(
            final Set<Key> worldKeys,
            final @KeyPattern.Namespace String variantId
    ) {
        return (variants, ctx) -> {
            if (!variants.containsKey(variantId)) throw unknownVariant(variants, ctx, variantId);
            final NamespacedKey currentWorldKey = ctx.getEntity().getLocation().getWorld().getKey();
            return (worldKeys.contains(currentWorldKey)) ? variants.get(variantId) : null;
        };
    }

    public static VariantRule ifTemperature(
            final DoubleRange temperatureRange,
            final @KeyPattern.Namespace String variantId
    ) {
        return (variants, ctx) -> {
            if (!variants.containsKey(variantId)) throw unknownVariant(variants, ctx, variantId);
            final Location location = ctx.getEntity().getLocation();
            final double temperature = location.getWorld().getTemperature(
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ()
            );
            return temperatureRange.contains(temperature) ? variants.get(variantId) : null;
        };
    }

    public static VariantRule firstMatch(final VariantRule... rules) {
        return (variants, ctx) -> {
            for (final VariantRule rule : rules) {
                final BoundEntityVariant variant = rule.apply(variants, ctx);
                if (variant != null) return variant;
            }
            return null;
        };
    }
}
