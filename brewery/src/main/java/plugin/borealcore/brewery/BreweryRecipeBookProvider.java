package plugin.borealcore.brewery;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.borealcore.api.module.BorealGUI;
import plugin.borealcore.utility.DebugLevel;
import plugin.borealcore.utility.AdventureUtil;

import java.util.List;

public class BreweryRecipeBookProvider extends BorealGUI {
    private static ItemStack unknownRecipeStack;

    public BreweryRecipeBookProvider() {
        super(6, "Brewery Recipe Book");
        // unknownRecipeStack = build(ConfigManager.unknownBrewItem);
        // @TODO: add the config option in ConfigManager and create the item in ItemsAdder
    }

    @Override
    public void init(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);

        List<String> unlockedRecipes = getUnlockedBreweryRecipes(player); // @TODO re implement this to check your current permission system
        // I'll swap it for the db implemention later

        for (String recipe : BreweryModule.RECIPES.keySet()) {
            boolean hasRecipe = unlockedRecipes.contains(recipe);
            ItemStack itemStack;

            if (hasRecipe) {
                itemStack = buildRecipeItem(recipe, player);
            } else {
                itemStack = buildUnknownRecipeItem(recipe);
            }

            int slot = BreweryModule.RECIPES.get(recipe).getSlot(); // @TODO: see if you want to hard code the slots or use an iterator to assign them dynamically
            if (slot != -1) {
                int row = (slot - 1) / 9; // Calculate the row based on the slot
                int column = (slot - 1) % 9;  // Calculate the column based on the slot

                //contents.set(row, column, ClickableItem.of(itemStack, e -> handleItemClick(e, player, recipe, hasRecipe)));
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
        CustomStack customStack = CustomStack.getInstance(recipe + "_unknown"); // @TODO: decide on a naming convention for the unknown recipe items in ItemsAdder
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
