package plugin.borealcore.functions.cooking;

import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.borealcore.BorealCore;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.GuiUtil;

import java.util.ArrayList;
import java.util.List;

import static plugin.borealcore.manager.GuiManager.INGREDIENTS;
import static plugin.borealcore.utility.ItemUtil.build;

public class IngredientBookProvider { //@TODO important, migrate this to use a BorealGUI
    private final CookingManager cookingManager;

    public IngredientBookProvider() {
        this.cookingManager = BorealCore.getCookingManager();
    }

    public void update(Player player) {
        // Doesn't do anything yet, eventually will update ingredients?
    }

    public void init(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);

        for (String recipe : INGREDIENTS.keySet()) {
            ItemStack itemStack;
            itemStack = buildRecipeItem(recipe, player);

            int slot = INGREDIENTS.get(recipe).getSlot(); // Retrieve the slot from the configuration
            if (slot != -1) {
                int row = (slot - 1) / 9; // Calculate the row based on the slot
                int column = (slot - 1) % 9;  // Calculate the column based on the slot
            }
        }
    }

    private ItemStack buildRecipeItem(String recipe, Player player) {
        CustomStack customStack = CustomStack.getInstance(recipe);
        if (customStack == null) {
            return build(CookingConfig.unknownItem);
        } else {
            ItemStack stack = customStack.getItemStack();
            modifyLore(stack, player, recipe);
            return stack;
        }
    }

    private void modifyLore(ItemStack itemStack, Player player, String recipe) {
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

        if (INGREDIENTS.get(recipe).getIngredients() != null) {
            GuiUtil.appendIngredients(lore, player, INGREDIENTS.get(recipe).getIngredients());
        }

        lore.add(" ");
        lore.add(CookingConfig.cookLine);
        lore.add(CookingConfig.cookLineShift);

        // Create a new list to store parsed lore
        List<Component> parsedLore = new ArrayList<>();

        // Parse each lore line and add it to the parsedLore list
        for (String line : lore) {
            parsedLore.add(AdventureUtil.getComponentFromMiniMessage(line));
        }

        itemMeta.lore(parsedLore);
        itemStack.setItemMeta(itemMeta);
    }

    private void handleItemClick(InventoryClickEvent event, Player player, String recipe) {
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            if (event.isShiftClick()) {
                // Shift click handling for cooking 15 Recipes
                cookingManager.handleMaterialAutocooking(recipe, player, 15);
            } else if (event.isLeftClick() || event.isRightClick()) {
                // Left-click handling logic for autocooking the recipe
                cookingManager.handleMaterialAutocooking(recipe, player, 1);
            }
        }
        event.setCancelled(true);
    }
}