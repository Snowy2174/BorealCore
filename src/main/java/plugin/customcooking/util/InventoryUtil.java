package plugin.customcooking.util;


import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.customcooking.manager.EffectManager;
import plugin.customcooking.manager.configs.ConfigManager;

import java.util.List;

public class InventoryUtil {

    public InventoryUtil() {}

    public static boolean handleIngredientCheck(Inventory playerInventory, List<String> ingredients, Integer instances) {
        if (ingredients == null || ingredients.isEmpty()) {
            return true; // consider inventory as having all ingredients if list is empty
        }
        for (String ingredientString : ingredients) {
            String[] options = ingredientString.split("/");
            boolean optionFound = handleOptions(playerInventory, options, instances);
            if (!optionFound) {
                return false; // option not found in player's inventory, return false immediately
            }
        }
        return true; // all ingredients found in player's inventory for at least one option
    }

    private static boolean handleOptions(Inventory playerInventory, String[] options, Integer instances) {
        for (String option : options) {
            String[] parts = option.split(":");
            int amount = Integer.parseInt(parts[1]) * instances;

            if (parts[0].endsWith("*")) {
                if (tieredIngredientCheck(playerInventory, parts[0].replace("*", ""), amount)) {
                    return true; // ingredient found in player's inventory for this option
                }
            } else {
                CustomStack customStack = CustomStack.getInstance(parts[0]);
                if (customStack != null) {
                    ItemStack itemStack = customStack.getItemStack();
                    if (playerInventory.containsAtLeast(itemStack, amount)) {
                        return true; // ingredient found in player's inventory for this option
                    }
                } else {
                    Material material = Material.getMaterial(parts[0]);
                    if (material != null && playerInventory.containsAtLeast(new ItemStack(material), amount)) {
                        return true; // ingredient found in player's inventory for this option
                    }
                }
            }
        }
        return false; // none of the options found in player's inventory
    }


    public static boolean tieredIngredientCheck(Inventory playerInventory, String ingredient, Integer amount) {
        CustomStack customStack = CustomStack.getInstance(ingredient);
        if (customStack == null) {
            return false; // Invalid ingredient
        }

        for (int tier = 0; tier <= 2; tier++) {
            String tieredIngredient = ingredient + (tier > 0 ? "_t" + tier : "");
            CustomStack customStackTiered = CustomStack.getInstance(tieredIngredient);
            ItemStack itemStackTiered = customStackTiered.getItemStack();
            if (playerInventory.containsAtLeast(itemStackTiered, amount)) {
                return true; // Found ingredient in player's inventory
            }
        }
        return false; // Ingredient not found in player's inventory
    }

    public static void removeIngredients(Inventory playerInventory, List<String> ingredients, Integer instances) {
        if (ingredients == null || ingredients.isEmpty()) {
            return; // No ingredients to remove
        }

        for (String ingredient : ingredients) {
            String[] options = ingredient.split("/");
            for (String option : options) {
                String[] parts = option.split(":");
                String ingredientName = parts[0];
                int amount = Integer.parseInt(parts[1]) * instances;

                if (ingredientName.endsWith("*")) {
                    if (playerHasIngredient(playerInventory, ingredientName)) {
                        removeTieredIngredient(playerInventory, ingredientName, amount);
                        break; // Exit the loop after removing one tiered ingredient
                    }
                } else {
                    if (playerHasIngredient(playerInventory, ingredientName)) {
                        removeNonTieredIngredient(playerInventory, ingredientName, amount);
                        break; // Exit the loop after removing one non-tiered ingredient
                    }
                }
            }
        }
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
                if (material != null) {
                    return playerInventory.containsAtLeast(new ItemStack(material), 1);
                } else {
                    return false;
                }
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

    public static void giveItem(Player player, String item, Integer amount, boolean customCookingItem) {
        ItemStack drop = build(item);
        drop.setAmount(amount);
        if (customCookingItem) {

            EffectManager.addPotionEffectLore(drop, item, item.contains(ConfigManager.perfectItemSuffix));
            addIdentifier(drop, item);
        }
        player.getLocation().getWorld().dropItem(player.getLocation(), drop);
    }

    @Nullable
    public static ItemStack buildia(String key) {
        if (key == null) {
            return null;
        }
        String material = key.replaceAll("[\\[\\]]", "");
        CustomStack customStack = CustomStack.getInstance(material);
        return customStack == null ? null : customStack.getItemStack();
    }

    @NotNull
    public static ItemStack build(String key) {
        ItemStack itemStack = buildia(key);
        if (itemStack == null) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                itemStack = new ItemStack(material);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid material name: " + key);
                itemStack = new ItemStack(Material.AIR);
            }
        }
        return itemStack;
    }

    private static void addIdentifier(ItemStack itemStack, String id){
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.addCompound("CustomCooking");
        String identifier = id.replaceAll("[\\[\\]]", "");
        nbtCompound.setString("id", identifier);
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }
}
