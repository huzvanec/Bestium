package cz.jeme.bestium.api.util;

import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

/**
 * Utility methods for building Minecraft {@link LootTable}s.
 */
public final class LootTableUtil {
    private LootTableUtil() {
        throw new AssertionError();
    }

    /**
     * Generic-capture helper method creating a {@link SetComponentsFunction} builder for a single {@link TypedDataComponent}.
     *
     * @param component the typed data component to copy onto the loot function
     * @param <T>       the component's value type
     * @return a loot item function builder that sets the given component
     */
    private static <T> LootItemConditionalFunction.Builder<?> createSetComponentFun(final TypedDataComponent<T> component) {
        return SetComponentsFunction.setComponent(component.type(), component.value());
    }

    /**
     * Creates a {@link UniformContainerBase.Builder} from a Bukkit {@link Material}.
     * <p>
     * Example usage:
     * <pre>{@code
     * LootTable.lootTable()
     *     .withPool(
     *         LootPool.lootPool()
     *             // ...
     *             .add(LootTableUtil.lootTableItem(Material.DIAMOND_SWORD)
     *                 .apply(
     *                     SetItemCountFunction.setCount(UniformGenerator.between(0F, 1F))
     *                 )
     *             )
     *     )
     *     // ...
     *     .build();
     * }</pre>
     *
     * @param material the Bukkit {@link Material} to create the loot table item for
     * @return a builder for a loot table item corresponding to the specified material
     */
    public static UniformContainerBase.Builder<?> lootTableItem(final Material material) {
        return LootItem.lootTableItem(CraftItemType.bukkitToMinecraft(material));
    }

    /**
     * Creates a {@link UniformContainerBase.Builder} from a Bukkit {@link ItemType}.
     * <p>
     * This is primarily a helper for items registered by <a href="https://docs.xenondevs.xyz/nova/addon/items/overview/">Nova Addons</a>
     * <p>
     * Example usage:
     * <pre>{@code
     * LootTable.lootTable()
     *     .withPool(
     *         LootPool.lootPool()
     *             // ...
     *             .add(LootTableUtil.lootTableItem(MyAddonItems.RUBY_SWORD.get())
     *                 .apply(
     *                     SetItemCountFunction.setCount(UniformGenerator.between(0F, 1F))
     *                 )
     *             )
     *     )
     *     // ...
     *     .build();
     * }</pre>
     *
     * @param itemType the Bukkit {@link ItemType} to create the loot table item for
     * @return a builder for a loot table item corresponding to the specified material
     */
    public static UniformContainerBase.Builder<?> lootTableItem(final ItemType itemType) {
        return LootItem.lootTableItem(CraftItemType.bukkitToMinecraftNew(itemType));
    }

    /**
     * Creates a {@link LootItem} builder from a Bukkit
     * {@link ItemStack}.
     * <p>
     * This method copies over the item type and all data components of the original
     * {@code ItemStack}.
     * <p>
     * The amount of items in the stack is ignored! If you wish to set the count, add a
     * {@link SetItemCountFunction} to the returned builder.
     * <p>
     * Example usage:
     * <pre>{@code
     * LootTable.lootTable()
     *     .withPool(
     *         LootPool.lootPool()
     *             // ...
     *             .add(LootTableUtil.lootTableItem(itemStack)
     *                 .apply(
     *                     SetItemCountFunction.setCount(UniformGenerator.between(0F, 1F))
     *                 )
     *             )
     *     )
     *     // ...
     *     .build();
     * }</pre>
     *
     * @param itemStack the Bukkit item stack to copy the type and components from
     * @return a loot item builder representing the given item, with all of its components applied
     */
    public static UniformContainerBase.Builder<?> lootTableItem(final ItemStack itemStack) {
        final var mojangStack = CraftItemStack.unwrap(itemStack);
        final var builder = LootItem.lootTableItem(mojangStack.getItem());

        for (final var component : mojangStack.getComponents()) {
            builder.apply(createSetComponentFun(component));
        }

        return builder;
    }
}
