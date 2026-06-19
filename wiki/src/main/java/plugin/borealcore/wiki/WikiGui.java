package plugin.borealcore.wiki;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.borealcore.api.module.BorealGUI;
import plugin.borealcore.utility.AdventureUtil;

import java.util.ArrayList;
import java.util.List;

public class WikiGui extends BorealGUI {

    public WikiGui() {
        // Initializes a 6-row (54 slot) inventory. Adjust the row count if your wiki is smaller.
        super(6, Component.text("Wiki"));
    }

    @Override
    public void init(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
        getInventory().clear(); // Ensure GUI is cleared if refreshed

        int slot = 11;
        for (String category : WikiModule.CATEGORY) {
            int row = slot / 9;
            int col = slot % 9;

            setItem(row, col, buildWikiItem(category), e -> handleItemClick(e, player, category));

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

        List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();

        if (lore.isEmpty()) {
            lore.add("This item does not have lore! Configure it correctly in ItemsAdder!");
        }

        lore.add(" ");

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
                WikiModule.openCategory(player, entry);
            }
        }

        // Note: event.setCancelled(true) is removed here because the BorealGUI
        // abstract class automatically cancels all clicks to prevent item stealing.
    }
}