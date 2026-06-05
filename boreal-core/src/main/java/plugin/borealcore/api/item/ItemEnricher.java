package plugin.borealcore.api.item;

import org.bukkit.inventory.ItemStack;

public interface ItemEnricher {
    /**
     * Checks if this module should apply extra data/lore to this specific item key.
     */
    boolean handles(String itemKey);

    /**
     * Modifies the ItemStack (e.g., adding Lore components, applying PersistentDataContainer tags).
     */
    void enrich(ItemStack itemStack, String itemKey);
}
