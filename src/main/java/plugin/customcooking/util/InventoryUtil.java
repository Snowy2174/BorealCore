package plugin.customcooking.util;

import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.customcooking.configs.RecipeManager;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.CookingManager;
import plugin.customcooking.minigame.Product;

import java.util.List;

import static net.kyori.adventure.key.Key.key;
import static plugin.customcooking.configs.RecipeManager.successItems;
import static plugin.customcooking.util.AdventureUtil.playerSound;

public class InventoryUtil {

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

            if (parts[0].endsWith("*")) {
                tieredIngredientCheck(playerInventory, parts[0].replace("*", ""), parts[1]);
            } else {
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
        }
        return true; // all ingredients found in player's inventory
    }

    public static boolean tieredIngredientCheck(Inventory playerInventory, String ingredient, String amount) {
        CustomStack customStack = CustomStack.getInstance(ingredient);
        if (customStack == null) {
            return false; // Invalid ingredient
        }

        for (int tier = 0; tier <= 2; tier++) {
            String tieredIngredient = ingredient + (tier > 0 ? "_t" + tier : "");
            CustomStack customStackTiered = CustomStack.getInstance(tieredIngredient);
            ItemStack itemStackTiered = customStackTiered.getItemStack();
            if (playerInventory.containsAtLeast(itemStackTiered, Integer.parseInt(amount))) {
                return true; // Found ingredient in player's inventory
            }
        }
        return false; // Ingredient not found in player's inventory
    }


    public static void removeIngredients(Inventory playerInventory, List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return; // No ingredients to remove
        }

        for (String ingredient : ingredients) {
            String[] parts = ingredient.split(":");
            String ingredientName = parts[0];
            int amount = Integer.parseInt(parts[1]);

            if (ingredientName.endsWith("*")) {
                removeTieredIngredient(playerInventory, ingredientName, amount);
            } else {
                removeNonTieredIngredient(playerInventory, ingredientName, amount);
            }
        }
    }

    private static void removeTieredIngredient(Inventory playerInventory, String ingredient, int amount) {
        String baseIngredient = ingredient.replace("*", "");
        for (int tier = 0; tier <= 2; tier++) {
            String tieredIngredient = baseIngredient + (tier > 0 ? "_t" + tier : "");
            CustomStack customStackTiered = CustomStack.getInstance(tieredIngredient);
            if (customStackTiered != null) {
                ItemStack itemStackTiered = customStackTiered.getItemStack();
                itemStackTiered.setAmount(amount);
                playerInventory.removeItem(itemStackTiered);
                return; // Exit the method after removing one tiered ingredient
            }
        }
    }

    private static void removeNonTieredIngredient(Inventory playerInventory, String ingredient, int amount) {
        CustomStack customStack = CustomStack.getInstance(ingredient);
        if (customStack != null) {
            ItemStack itemStack = customStack.getItemStack();
            itemStack.setAmount(amount);
            playerInventory.removeItem(itemStack);
        } else {
            Material material = Material.getMaterial(ingredient);
            if (material != null) {
                playerInventory.removeItem(new ItemStack(material, amount));
            }
        }
    }


    public static void giveItem(Player player, String item) {
        ItemStack drop = build(item);
        player.getLocation().getWorld().dropItem(player.getLocation(), drop);
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
        if (ingredient.endsWith("*")) {
            return hasTieredIngredient(playerInventory, ingredient);
        } else {
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

    private static boolean hasTieredIngredient(Inventory playerInventory, String ingredient) {
        String baseIngredient = ingredient.replace("*", "");
        for (int tier = 0; tier <= 2; tier++) {
            String tieredIngredient = baseIngredient + (tier > 0 ? "_t" + tier : "");
            CustomStack customStackTiered = CustomStack.getInstance(tieredIngredient);
            if (customStackTiered != null) {
                ItemStack itemStackTiered = customStackTiered.getItemStack();
                if (playerInventory.containsAtLeast(itemStackTiered, 1)) {
                    return true;
                }
            }
        }
        return false;
    }
}
