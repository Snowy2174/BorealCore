package plugin.borealcore.utility;


import dev.lone.itemsadder.api.CustomStack;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.configs.EffectManager;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.DebugLevel;

import java.util.List;

import static plugin.borealcore.functions.cooking.configs.RecipeManager.COOKING_RECIPES;

public class InventoryUtil {

    public InventoryUtil() {
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
                AdventureUtil.consoleMessage(DebugLevel.WARNING, "Invalid material name: " + key);
                itemStack = new ItemStack(Material.AIR);
            }
        }
        return itemStack;
    }

    private static void addIdentifier(ItemStack itemStack, String id) {
        NamespacedKey key = new NamespacedKey(BorealCore.getInstance(), "id");
        itemStack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        });
    }

    public static void giveItem(Player player, String item, Integer amount, boolean customCookingItem) {
        ItemStack drop = build(item);
        drop.setAmount(amount);
        if (customCookingItem) {
            EffectManager.addPotionEffectLore(drop, item, item.contains(ConfigManager.perfectItemSuffix));
            addIdentifier(drop, item.replace("[", "").replace("]", "")); // @TODO diagnose fix later
        }
        player.getLocation().getWorld().dropItem(player.getLocation(), drop);
    }

    private static void removeItemsPrecisely(Inventory inv, ItemStack target, int amount) {
        for (int i = 0; i < inv.getSize() && amount > 0; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || !item.isSimilar(target)) continue;
            int stackAmount = item.getAmount();
            if (stackAmount <= amount) {
                inv.setItem(i, null);
                amount -= stackAmount;
            } else {
                item.setAmount(stackAmount - amount);
                inv.setItem(i, item);
                amount = 0;
            }
        }
    }

    public static void removeItem(Inventory playerInventory, String ingredient, int amount) {
        if (ingredient.endsWith("*")) {
            System.out.println(ingredient);
            removeWildcardItem(playerInventory, ingredient, amount);
            return;
        }

        CustomStack customStack = CustomStack.getInstance(ingredient);
        if (customStack != null) {
            ItemStack itemStack = customStack.getItemStack();
            ItemStack clone = itemStack.clone();
            clone.setAmount(amount);
            playerInventory.removeItem(clone);
        } else {
            Material material = Material.getMaterial(ingredient);
            if (material != null) {
                playerInventory.removeItem(new ItemStack(material, amount));
            }
        }
    }

    public static ItemStack buildItemAPI(String key) {
        ItemStack itemStack = buildia(key);
        if (itemStack == null) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                itemStack = new ItemStack(material);
            } catch (IllegalArgumentException e) {
                AdventureUtil.consoleMessage(DebugLevel.WARNING, "Invalid material name: " + key);
                itemStack = new ItemStack(Material.AIR);
            }
        }
        if (COOKING_RECIPES.containsKey(key.toLowerCase().replace("_perfect", ""))) {
            EffectManager.addPotionEffectLore(itemStack, key, key.contains(ConfigManager.perfectItemSuffix));
            addIdentifier(itemStack, key);
        }
        return itemStack;
    }

    public static boolean handleIngredientCheck(Inventory playerInventory, List<String> ingredients, Integer instances) {
        if (ingredients == null || ingredients.isEmpty()) {
            return true;
        }
        for (String ingredientString : ingredients) {
            String[] options = ingredientString.split("/");
            if (!handleOptions(playerInventory, options, instances)) {
                return false;
            }
        }
        return true;
    }

    private static boolean handleOptions(Inventory playerInventory, String[] options, Integer instances) {
        for (String option : options) {
            String[] parts = option.split(":");
            int amount;
            try {
                amount = Integer.parseInt(parts[1]) * instances;
            } catch (NumberFormatException e) {
                AdventureUtil.consoleMessage(DebugLevel.WARNING, "Invalid ingredient amount: " + parts[1]);
                continue;
            }

            if (parts[0].endsWith("*")) {
                if (tieredIngredientCheck(playerInventory, parts[0].replace("*", ""), amount)) {
                    return true;
                }
                if (fishingIngredientCheck(playerInventory, parts[0].replace("*", ""), amount)) {
                    return true;
                }
            } else {
                CustomStack customStack = CustomStack.getInstance(parts[0]);
                if (customStack != null) {
                    ItemStack itemStack = customStack.getItemStack();
                    if (playerInventory.containsAtLeast(itemStack, amount)) {
                        return true;
                    }
                } else {
                    Material material = Material.getMaterial(parts[0]);
                    if (material != null && playerInventory.containsAtLeast(new ItemStack(material), amount)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean tieredIngredientCheck(Inventory playerInventory, String ingredient, Integer amount) {
        CustomStack customStack = CustomStack.getInstance(ingredient);
        if (customStack == null) {
            return false;
        }
        for (int tier = 0; tier <= 2; tier++) {
            String tieredIngredient = ingredient + (tier > 0 ? "_t" + tier : "");
            CustomStack customStackTiered = CustomStack.getInstance(tieredIngredient);
            ItemStack itemStackTiered = customStackTiered.getItemStack();
            if (playerInventory.containsAtLeast(itemStackTiered, amount)) {
                return true;
            }
        }
        return false;
    }

    public static boolean fishingIngredientCheck(Inventory playerInventory, String ingredient, Integer amount) {
        if (!ingredient.startsWith("fish")) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Ingredient does not start with 'fish': " + ingredient);
            return false;
        }

        int fishAmount = 0;
        for (ItemStack item : playerInventory.getContents()) {
            if (item == null) continue;
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Checking item: " + item.getType() + " x" + item.getAmount());
            String customFishingItemID = BukkitCustomFishingPlugin.getInstance().getItemManager().getCustomFishingItemID(item);
            if (customFishingItemID != null) {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Custom fishing item found: " + customFishingItemID);
                if (item.getAmount() >= amount) {
                    AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Enough fish found: " + item.getAmount());
                    return true;
                }
                fishAmount += item.getAmount();
            } else {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "No custom fishing item found for item: " + item);
            }
        }
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Total fish amount accumulated: " + fishAmount);
        return fishAmount >= amount;
    }

    public static void removeIngredients(Inventory playerInventory, List<String> ingredients, Integer instances) {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        for (String ingredient : ingredients) {
            String[] options = ingredient.split("/");
            for (String option : options) {
                String[] parts = option.split(":");
                String ingredientName = parts[0];
                if (parts.length < 2) {
                    AdventureUtil.consoleMessage(DebugLevel.WARNING, "Malformed ingredient: " + option);
                    continue;
                }
                int amount = Integer.parseInt(parts[1]) * instances;
                removeItem(playerInventory, ingredientName, amount);
            }
        }
    }

    public static boolean playerHasIngredient(Inventory playerInventory, String ingredient) {
        if (ingredient.endsWith("*")) {
            String base = ingredient.replace("*", "");
            if (base.startsWith("fish")) {
                return fishingIngredientCheck(playerInventory, base, 1);
            } else {
                return tieredIngredientCheck(playerInventory, ingredient, 1);
            }
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

    static void removeWildcardItem(Inventory playerInventory, String ingredient, int amount) {
        String baseIngredient = ingredient.replace("*", "");
        if (baseIngredient.startsWith("fish")) {
            int remaining = amount;
            for (int i = 0; i < playerInventory.getSize(); i++) {
                ItemStack item = playerInventory.getItem(i);
                if (item == null) continue;
                String customFishingItemID = BukkitCustomFishingPlugin.getInstance().getItemManager().getCustomFishingItemID(item);
                if (customFishingItemID != null) {
                    int itemAmount = item.getAmount();
                    if (itemAmount <= remaining) {
                        playerInventory.setItem(i, null);
                        remaining -= itemAmount;
                    } else {
                        item.setAmount(itemAmount - remaining);
                        playerInventory.setItem(i, item);
                        remaining = 0;
                    }
                    if (remaining <= 0) break;
                }
            }
            return;
        }

        for (int tier = 0; tier <= 2; tier++) {
            if (amount <= 0) break;
            String tieredIngredient = baseIngredient + (tier > 0 ? "_t" + tier : "");
            CustomStack customStackTiered = CustomStack.getInstance(tieredIngredient);
            if (customStackTiered != null) {
                ItemStack itemStackTiered = customStackTiered.getItemStack();
                int availableAmount = 0;
                for (ItemStack item : playerInventory.getContents()) {
                    if (item != null && item.isSimilar(itemStackTiered)) {
                        availableAmount += item.getAmount();
                    }
                }
                int toRemove = Math.min(amount, availableAmount);
                if (toRemove > 0) {
                    removeItemsPrecisely(playerInventory, itemStackTiered, toRemove);
                    amount -= toRemove;
                }
            } else {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "CustomStack for " + tieredIngredient + " is null.");
            }
        }
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Finished removing tiered or fish item: " + ingredient);
    }
}
