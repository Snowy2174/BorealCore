package plugin.borealcore.market;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.api.module.BorealGUI;
import plugin.borealcore.object.DebugLevel;
import plugin.borealcore.utility.AdventureUtil;

import java.util.ArrayList;
import java.util.List;

public class CategoryGUI extends BorealGUI {

    private final String category;
    private final int rows;
    private final List<ItemContext> items;

    // Pagination tracking
    private int page = 0;

    public CategoryGUI(String category, int rows, List<ItemContext> items) {
        super(rows, Component.text(category));
        this.category = category;
        this.rows = rows;
        this.items = items;
    }

    @Override
    public void open(Player p) {
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Opening category " + category + " for " + p.getName());
        // super.open(p) natively calls init(p) and opens the inventory
        super.open(p);
    }

    @Override
    public void init(Player player) {
        // Clear previous items to prevent page overlap
        getInventory().clear();

        // 1. Draw Borders (Top and Bottom Rows)
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) {
            setItem(0, i, filler, null);
            setItem(rows - 1, i, filler, null);
        }

        // 2. Back Button
        setItem(rows - 1, 0, new ItemStack(Material.ARROW), e -> {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, player.getName() + " returned to main menu");
            new MarketGUI("Shop", 6).open(player);
        });

        // 3. Populate Items & Handle Pagination
        if (items != null && !items.isEmpty()) {
            int itemsPerPage = (rows - 2) * 9; // Available slots inside the top/bottom borders
            int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);

            // Boundary safety
            if (page >= totalPages) page = Math.max(0, totalPages - 1);

            int startIndex = page * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, items.size());

            // Start placing items on Row 1 (skipping Row 0 filler)
            int currentRow = 1;
            int currentCol = 0;

            for (int i = startIndex; i < endIndex; i++) {
                ItemContext ctx = items.get(i);
                ItemStack disp = ctx.getDisplay().clone();

                // Construct Lore
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Buy: " + ctx.getBuyPrice()).color(NamedTextColor.GREEN));
                lore.add(Component.text("Sell: " + ctx.getSellPrice()).color(NamedTextColor.YELLOW));
                disp.editMeta(meta -> meta.lore(lore));

                // Add item to GUI grid
                setItem(currentRow, currentCol, disp, e -> {
                    AdventureUtil.consoleMessage(DebugLevel.DEBUG, player.getName() + " clicked item " + ctx.getId());
                    // placeholder buy/sell action
                    player.sendMessage(Component.text("Clicked " + ctx.getId()));
                });

                // Advance Grid coordinates
                currentCol++;
                if (currentCol > 8) {
                    currentCol = 0;
                    currentRow++;
                }
            }

            // 4. Pagination Navigation Arrows
            if (totalPages > 1) {
                if (page > 0) {
                    setItem(rows - 1, 3, new ItemStack(Material.ARROW), e -> {
                        this.page--;
                        init(player); // Refresh GUI contents dynamically
                    });
                }

                if (page < totalPages - 1) {
                    setItem(rows - 1, 5, new ItemStack(Material.ARROW), e -> {
                        this.page++;
                        init(player); // Refresh GUI contents dynamically
                    });
                }

                // Page Indicator (I added the page numbering into the Paper's meta for better UX)
                ItemStack pageIndicator = new ItemStack(Material.PAPER);
                pageIndicator.editMeta(meta -> meta.displayName(Component.text("Page " + (page + 1) + "/" + totalPages).color(NamedTextColor.WHITE)));
                setItem(rows - 1, 4, pageIndicator, null);
            }
        }
    }
}