package plugin.customcooking.functions.wiki;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.customcooking.BorealCore;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.utility.AdventureUtil;

import java.util.ArrayList;
import java.util.List;

public class WikiGuiProvider implements InventoryProvider {
    private final WikiManager wikiManager;

    public WikiGuiProvider() {
        this.wikiManager = BorealCore.getWikiManager();
    }

    @Override

    public void update(Player player, InventoryContents contents) {
        // Doesn't do anything yet, eventually will update ingredients?
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);

        int slot = 11;
        for (String category : WikiManager.CATEGORY) {
            int row = slot / 9;
            int col = slot % 9;
            contents.set(row, col, ClickableItem.of(buildWikiItem(category), e -> handleItemClick(e, player, category)));
            slot += 2;
            if (col >= 7) {
                slot += 2;
            }
        }
    }

    private ItemStack buildWikiItem(String entry) {
        ItemStack stack = new ItemStack(Material.BOOK);
        modifyLore(stack, entry);
        return stack;
    }

    private void modifyLore(ItemStack itemStack, String entry) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
            itemStack.setItemMeta(itemMeta);
        }
        List<String> lore = itemMeta.getLore();

        if (!itemMeta.hasLore()) {
            lore = new ArrayList<>();
            lore.add("This item does not have lore! Configure it correctly in ItemsAdder!");
        }

        lore.add(" ");
        lore.add(ConfigManager.cookLine);
        lore.add(ConfigManager.cookLineShift);

        // Create a new list to store parsed lore
        List<Component> parsedLore = new ArrayList<>();

        // Parse each lore line and add it to the parsedLore list
        for (String line : lore) {
            parsedLore.add(AdventureUtil.getComponentFromMiniMessage(line));
        }

        itemMeta.lore(parsedLore);
        itemMeta.displayName(Component.text(entry));
        itemStack.setItemMeta(itemMeta);
    }

    private void handleItemClick(InventoryClickEvent event, Player player, String entry) {
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            if (event.isLeftClick() || event.isRightClick()) {
                // Left-click handling logic for autocooking the recipe
                WikiManager.openCategory(player, entry);
            }
        }
        event.setCancelled(true);
    }
}