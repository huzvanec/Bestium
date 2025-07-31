package cz.jeme.bestium.api.inject.variant;

import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.DoubleRange;
import org.bukkit.Location;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

final class VariantPickers {
    private VariantPickers() {
        throw new AssertionError();
    }

    public static final VariantPicker FIRST = (variants, ctx) -> {
        final Iterator<BoundEntityVariant> it = variants.values().iterator();
        return it.hasNext() ? it.next() : null;
    };

    public static VariantPicker random() {
        final List<BoundEntityVariant> variantList = new ArrayList<>();

        return (variants, ctx) -> {
            if (variants.isEmpty()) return null;
            if (variantList.isEmpty()) variantList.addAll(variants.values());
            final int variantIndex = ThreadLocalRandom.current().nextInt(variants.size());
            return variantList.get(variantIndex);
        };
    }

    public static VariantPicker randomWithChance(final Map<String, Integer> chanceMap) {
        final List<String> valuedList = new ArrayList<>();
        for (final Map.Entry<String, Integer> entry : chanceMap.entrySet()) {
            final String variantId = entry.getKey();

            for (int i = 0; i < entry.getValue(); i++) {
                valuedList.add(variantId);
            }
        }
        final int size = valuedList.size();
        return (variants, ctx) -> {
            if (variants.isEmpty()) return null;
            final int variantIndex = ThreadLocalRandom.current().nextInt(size);
            final String variantId = valuedList.get(variantIndex);
            final BoundEntityVariant variant = variants.get(variantId);
            if (variant == null) throw new IllegalArgumentException(
                    "Unknown variant: '" + variantId + "' for entity '" + ctx.getRealEntityType() + "'"
            );
            return variant;
        };
    }

    private static <T> Map<T, String> invertAndFlattenSetMap(final Map<String, Set<T>> map) {
        final Map<T, String> invertedMap = new HashMap<>();
        map.forEach(
                (id, set) -> set.forEach(
                        t -> invertedMap.put(t, id)
                )
        );
        return invertedMap;
    }

    private static <T> VariantPicker bySwitch(
            final Function<VariantPicker.Context, T> accessor,
            final Map<String, Set<T>> switchMap,
            final @Nullable String fallback
    ) {
        final Map<T, String> invertedMap = invertAndFlattenSetMap(switchMap);
        return (variants, ctx) -> {
            if (variants.isEmpty()) return null;
            final String variantId = invertedMap.get(accessor.apply(ctx));
            final BoundEntityVariant variant = variants.get(variantId == null ? fallback : variantId);
            if (variant == null) throw new IllegalArgumentException(
                    "Unknown variant: '" + variantId + "' for entity '" + ctx.getRealEntityType() + "'"
            );
            return variant;
        };
    }

    public static VariantPicker byBiomeKey(
            final Map<String, Set<Key>> biomeMap,
            final @Nullable String fallback
    ) {
        return bySwitch(
                ctx -> ctx.getLocation().getWorld().getBiome(ctx.getLocation()).getKey(),
                biomeMap,
                fallback
        );
    }


    public static VariantPicker byWorldKey(
            final Map<String, Set<Key>> keyMap,
            final @Nullable String fallback
    ) {
        return bySwitch(
                ctx -> ctx.getLocation().getWorld().getKey(),
                keyMap,
                fallback
        );
    }

    public static VariantPicker byBiomeTemperature(
            final Map<String, DoubleRange> temperatureMap,
            final @Nullable String fallback
    ) {
        return (variants, ctx) -> {
            if (variants.isEmpty()) return null;
            final Location loc = ctx.getLocation();
            final double temp = loc.getWorld().getTemperature(
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ()
            );
            final String variantId = temperatureMap.entrySet().stream()
                    .filter(entry -> entry.getValue().contains(temp))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(fallback);
            final BoundEntityVariant variant = variants.get(variantId);
            if (variant == null) throw new IllegalArgumentException(
                    "Unknown variant: '" + variantId + "' for entity '" + ctx.getRealEntityType() + "'"
            );
            return variant;
        };
    }
}
