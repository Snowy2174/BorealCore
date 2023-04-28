package plugin.customcooking.Util;

import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.customcooking.Configs.RecipeManager;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.Manager.CookingManager;
import plugin.customcooking.Minigame.Product;

import java.util.List;

import static net.kyori.adventure.key.Key.key;
import static plugin.customcooking.Configs.RecipeManager.successItems;
import static plugin.customcooking.Util.AdventureUtil.playerSound;

public class InventoryUtil{

    private final CookingManager cookingManager;

    public InventoryUtil() {
        this.cookingManager = CustomCooking.getCookingManager();
    }

    public void ingredientCheck(Player player, String recipe, Boolean auto) {
        if (cookingManager.cookingPlayerCache.get(player) != null) {
            AdventureUtil.playerMessage(player, "<grey>[<bold><red>!</bold><grey>] <red>You're already cooking something.");
        } else {
            // check if player has required ingredients
            List<String> ingredients = RecipeManager.itemIngredients.get(recipe);
            // get the bar config
            Product bar = RecipeManager.RECIPES.get(recipe);
            //checks if player has required ingredients
            if (playerHasIngredients(player.getInventory(), ingredients)) {
                removeIngredients(player.getInventory(), ingredients);
                playerSound(player, Sound.Source.AMBIENT, key("customfoods", "cooking.ingredient"), 1f, 1f);
                if (auto) {
                    giveItem(player, String.valueOf(successItems.get(recipe)));
                    playerSound(player, Sound.Source.AMBIENT, key("customfoods", "done"), 1f, 1f);
                    AdventureUtil.playerMessage(player, "<gray>[<green><bold>!</bold><gray>] <green>You have auto-cooked one " + recipe);
                } else {
                    cookingManager.onCookedItem(player, bar);
                }
            } else {
                AdventureUtil.playerMessage(player, "<grey>[<bold><red>!</bold><grey>] <red>You do not have the required ingredients to cook this item.</red>");
            }
        }
    }

    public static boolean playerHasIngredients(Inventory playerInventory, List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return true; // consider inventory as having all ingredients if list is empty
        }
        for (String ingredientString : ingredients) {
            String[] parts = ingredientString.split(":");
            CustomStack customStack = CustomStack.getInstance(parts[0]);
            if (customStack != null) {
                ItemStack itemStack = customStack.getItemStack();
                int amount = Integer.parseInt(parts[1]);
                if (!playerInventory.containsAtLeast(itemStack, amount)) {
                    return false; // ingredient not found in player's inventory
                }
            } else {
                Material material = Material.getMaterial(parts[0]);
                if (material == null) {
                    return false; // invalid material name
                }
                int amount = Integer.parseInt(parts[1]);
                if (!playerInventory.contains(material, amount)) {
                    return false; // ingredient not found in player's inventory
                }
            }
        }
        return true; // all ingredients found in player's inventory
    }


    public static void removeIngredients(Inventory playerInventory, List<String> ingredients) {
        for (String ingredient : ingredients) {
            String[] parts = ingredient.split(":");
            CustomStack customStack = CustomStack.getInstance(parts[0]);
            if (customStack != null) {
                ItemStack itemStack = customStack.getItemStack();
                itemStack.setAmount(Integer.parseInt(parts[1]));
                playerInventory.removeItem(itemStack);
            } else {
                Material material = Material.getMaterial(parts[0]);
                int amount = Integer.parseInt(parts[1]);
                playerInventory.removeItem(new ItemStack(material, amount));
            }
        }
    }

    public static void giveItem(Player player, String item) {
        ItemStack drop = build(item);
        player.getInventory().addItem(drop);
    }

    @Nullable
    public static ItemStack buildia(String key) {
        String material = key.replaceAll("[\\[\\]]", "");
        CustomStack customStack = CustomStack.getInstance(material);
        return customStack == null ? null : customStack.getItemStack();
    }

    @NotNull
    public static ItemStack build(String key) {
        ItemStack itemStack = buildia(key);
        return itemStack == null ? new ItemStack(Material.AIR) : itemStack;
    }

    public static boolean playerHasIngredient(Inventory playerInventory, String ingredient) {
        CustomStack customStack = CustomStack.getInstance(ingredient);
        if (customStack != null) {
            ItemStack itemStack = customStack.getItemStack();
            return playerInventory.containsAtLeast(itemStack, 1);
        } else {
            Material material = Material.getMaterial(ingredient);
            return playerInventory.contains(material);
        }
    }
}
