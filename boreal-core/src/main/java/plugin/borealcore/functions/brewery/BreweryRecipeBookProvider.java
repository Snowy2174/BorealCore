package plugin.borealcore.functions.brewery;

import dev.lone.itemsadder.api.CustomStack;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.utility.AdventureUtil;

import java.util.List;

public class BreweryRecipeBookProvider implements InventoryProvider {
    private static ItemStack unknownRecipeStack;

    public BreweryRecipeBookProvider() {
        // unknownRecipeStack = build(ConfigManager.unknownBrewItem);
        // @TODO: add the config option in ConfigManager and create the item in ItemsAdder
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        // not really useful lets be honest
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
        contents.fill(ClickableItem.of(unknownRecipeStack,
                e -> AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.recipeUnknown)));
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.AIR)));

        List<String> unlockedRecipes = getUnlockedBreweryRecipes(player); // @TODO re implement this to check your current permission system
        // I'll swap it for the db implemention later

        for (String recipe : BreweryManager.RECIPES.keySet()) {
            boolean hasRecipe = unlockedRecipes.contains(recipe);
            ItemStack itemStack;

            if (hasRecipe) {
                itemStack = buildRecipeItem(recipe, player);
            } else {
                itemStack = buildUnknownRecipeItem(recipe);
            }

            int slot = BreweryManager.RECIPES.get(recipe).getSlot(); // @TODO: see if you want to hard code the slots or use an iterator to assign them dynamically
            if (slot != -1) {
                int row = (slot - 1) / 9; // Calculate the row based on the slot
                int column = (slot - 1) % 9;  // Calculate the column based on the slot

                contents.set(row, column, ClickableItem.of(itemStack, e -> handleItemClick(e, player, recipe, hasRecipe)));
            }
        }
    }

    private ItemStack buildRecipeItem(String recipe, Player player) {
        // @TODO: implement this however you like
        return null;
    }

    private List<String> getUnlockedBreweryRecipes(Player player) {
        return null;
    }

    private ItemStack buildUnknownRecipeItem(String recipe) {
        CustomStack customStack = CustomStack.getInstance(recipe + ConfigManager.unknownItemSuffix);
        if (customStack == null) {
            return unknownRecipeStack;
        } else {
            ItemStack stack = customStack.getItemStack();
            ItemMeta itemMeta = stack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setLore(unknownRecipeStack.getItemMeta().getLore());
                stack.setItemMeta(itemMeta);
            } else {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "ItemMeta is null!");
            }
            return stack;
        }
    }

    private void handleItemClick(InventoryClickEvent event, Player player, String recipe, boolean hasRecipe) {
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            // @TODO: idk choose what you want to happen if the player clicks on a recipe item
            event.setCancelled(true);
        }
    }
}
