package plugin.borealcore.functions.BorealExtras.market;

import org.bukkit.inventory.ItemStack;

public class ItemContext {
    private final ItemStack display;
    private final double buyPrice;
    private final double sellPrice;
    private final String id;

    public ItemContext(String id, ItemStack display, double buyPrice, double sellPrice) {
        this.id = id;
        this.display = display;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    public String getId() {
        return id;
    }

    public ItemStack getDisplay() {
        return display;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }
}
