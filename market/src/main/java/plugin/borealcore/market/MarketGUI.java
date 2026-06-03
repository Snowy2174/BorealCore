package plugin.borealcore.market;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.api.module.BorealGUI;
import plugin.borealcore.object.DebugLevel;
import plugin.borealcore.utility.AdventureUtil;

import java.util.List;

public class MarketGUI extends BorealGUI {

    private final int rows;

    public MarketGUI(String title, int rows) {
        super(rows, Component.text(title));
        this.rows = rows;
    }

    @Override
    public void open(Player p) {
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Opening main shop menu for " + p.getName());
        super.open(p);
    }

    @Override
    public void init(Player player) {
        getInventory().clear();

        List<String> categories = MarketModule.getCategories();
        int slot = 0;

        for (String cat : categories) {
            // Calculate grid placement (Starts at Row 1, Col 0)
            int r = 1 + slot / 9;
            int c = slot % 9;

            if (r >= rows - 1) break;

            ItemStack display = new ItemStack(Material.CHEST);
            display.editMeta(meta -> meta.displayName(Component.text(cat).color(NamedTextColor.WHITE)));

            setItem(r, c, display, e -> {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, player.getName() + " opened category " + cat);
                new CategoryGUI(cat, 6, MarketModule.getItems(cat)).open(player);
            });

            slot++;
        }
    }
}