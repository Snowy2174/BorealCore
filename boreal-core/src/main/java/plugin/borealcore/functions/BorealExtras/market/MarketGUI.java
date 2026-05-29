package plugin.borealcore.functions.BorealExtras.market;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.utility.AdventureUtil;

import java.util.ArrayList;
import java.util.List;

public class MarketGUI implements InventoryProvider {
    private final SmartInventory inventory;
    private final String title;
    private final int rows;

    public MarketGUI(String title, int rows) {
        this.title = title;
        this.rows = rows;
        this.inventory = SmartInventory.builder()
                .provider(this)
                .size(rows, 9)
                .title(title)
                .manager(BorealCore.getInventoryManager())
                .build();
    }

    public void open(Player p) {
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Opening main shop menu for " + p.getName());
        inventory.open(p);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        List<ClickableItem> items = new ArrayList<>();
        for (String cat : MarketManager.getCategories()) {
            ItemStack display = new ItemStack(Material.CHEST);
            // set name
            display.editMeta(meta -> meta.displayName(Component.text(cat).color(NamedTextColor.WHITE)));
            items.add(ClickableItem.of(display, e -> {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, player.getName() + " opened category " + cat);
                new CategoryGUI(cat, 6, MarketManager.getItems(cat)).open(player);
            }));
        }

        // Place items starting at row 1 col 0 horizontally
        int slot = 0;
        for (ClickableItem ci : items) {
            int r = 1 + slot / 9;
            int c = slot % 9;
            if (r >= rows - 1) break;
            contents.set(r, c, ci);
            slot++;
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}