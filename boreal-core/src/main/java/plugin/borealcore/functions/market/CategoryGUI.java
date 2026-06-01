package plugin.borealcore.functions.market;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.*;
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

public class CategoryGUI implements InventoryProvider {
    private final String category;
    private final int rows;
    private final List<ItemContext> items;
    private final SmartInventory inventory;

    public CategoryGUI(String category, int rows, List<ItemContext> items) {
        this.category = category;
        this.rows = rows;
        this.items = items;
        this.inventory = SmartInventory.builder()
                .provider(this)
                .size(rows, 9)
                .title(category)
                .manager(BorealCore.getInventoryManager())
                .build();
    }

    public void open(Player p) {
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Opening category " + category + " for " + p.getName());
        inventory.open(p);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) {
            contents.set(0, i, ClickableItem.empty(filler));
            contents.set(rows - 1, i, ClickableItem.empty(filler));
        }

        contents.set(rows - 1, 0, ClickableItem.of(new ItemStack(Material.ARROW), e -> {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, player.getName() + " returned to main menu");
            new MarketGUI("Shop", 6).open(player);
        }));

        List<ClickableItem> cis = new ArrayList<>();
        for (ItemContext ctx : items) {
            ItemStack disp = ctx.getDisplay().clone();
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Buy: " + ctx.getBuyPrice()).color(NamedTextColor.GREEN));
            lore.add(Component.text("Sell: " + ctx.getSellPrice()).color(NamedTextColor.YELLOW));
            disp.editMeta(meta -> meta.lore(lore));
            cis.add(ClickableItem.of(disp, e -> {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, player.getName() + " clicked item " + ctx.getId());
                // placeholder buy/sell action
                player.sendMessage(Component.text("Clicked " + ctx.getId()));
            }));
        }

        if (!cis.isEmpty()) {
            pagination.setItems(cis.toArray(new ClickableItem[0]));
            int itemsPerPage = (rows - 2) * 9;
            pagination.setItemsPerPage(itemsPerPage);
            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0)
                    .blacklist(0, 0).blacklist(0, 8)
                    .blacklist(rows - 1, 0).blacklist(rows - 1, 8));

            int totalPages = (int) Math.ceil((double) cis.size() / itemsPerPage);
            if (totalPages > 1) {
                if (!pagination.isFirst()) {
                    contents.set(rows - 1, 3, ClickableItem.of(new ItemStack(Material.ARROW),
                            e -> openPage(player, pagination.getPage() - 1, contents)));
                }
                if (!pagination.isLast()) {
                    contents.set(rows - 1, 5, ClickableItem.of(new ItemStack(Material.ARROW),
                            e -> openPage(player, pagination.getPage() + 1, contents)));
                }
                contents.set(rows - 1, 4, ClickableItem.empty(new ItemStack(Material.PAPER)));
            }
        }
    }

    private void openPage(Player player, int page, InventoryContents contents) {
        contents.pagination().page(page);
        contents.inventory().open(player, page);
    }

    @Override
    public void update(Player player, InventoryContents contents) {}
}
