package plugin.borealcore.manager;

import org.bukkit.inventory.ItemStack;
import plugin.borealcore.api.item.ItemEnricher;

import java.util.ArrayList;
import java.util.List;

public class ItemEnrichmentManager {

    private final List<ItemEnricher> enrichers = new ArrayList<>();

    public void register(ItemEnricher enricher) {
        this.enrichers.add(enricher);
    }

    /**
     * Passes the item through all registered modules that handle this key.
     * * @return true if at least one enricher modified the item, false otherwise.
     */
    public boolean processItem(ItemStack itemStack, String itemKey) {
        if (itemStack == null || itemStack.getType().isAir() || itemKey == null) {
            return false;
        }

        boolean wasEnriched = false;
        for (ItemEnricher enricher : enrichers) {
            if (enricher.handles(itemKey)) {
                enricher.enrich(itemStack, itemKey);
                wasEnriched = true; // Mark as successfully enriched
            }
        }

        return wasEnriched;
    }
}