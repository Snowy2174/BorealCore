package plugin.borealcore.utility;

import dev.lone.itemsadder.api.CustomStack;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.BorealCore;

import java.util.List;
import java.util.Objects;

public class ItemUtil {

    private ItemUtil() {}

    @Nullable
    public static ItemStack buildia(@Nullable String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String material = key.replaceAll("[\\[\\]]", "");
        CustomStack customStack = CustomStack.getInstance(material);
        return customStack == null ? null : customStack.getItemStack();
    }

    /**
     * Builds an ItemStack from a key.
     * @throws IllegalArgumentException if the key is invalid or the item cannot be built.
     */
    @NotNull
    public static ItemStack build(@NotNull String key) throws IllegalArgumentException {
        ItemStack itemStack = buildia(key);
        if (itemStack == null) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                itemStack = new ItemStack(material);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid material or ItemsAdder key: " + key, e);
            }
        }

        if (BorealCore.getInstance().getItemEnrichmentManager().processItem(itemStack, key)) {
            addIdentifier(itemStack, key.replaceAll("[\\[\\]]", ""));
        }

        return itemStack;
    }

    public static Double getItemValue(@NotNull Player player, @NotNull ItemStack itemStack) {
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(itemStack, "ItemStack cannot be null");
        return BukkitCustomFishingPlugin.getInstance().getMarketManager().getItemPrice(Context.player(player), itemStack);
    }

    private static void addIdentifier(@NotNull ItemStack itemStack, @NotNull String id) {
        NamespacedKey key = new NamespacedKey(BorealCore.getInstance(), "id");
        itemStack.editMeta(meta -> {
            if (meta != null) {
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
            }
        });
    }

    /**
     * Drops an item at the player's location.
     * @throws IllegalArgumentException if the item fails to build.
     */
    public static void giveItem(@NotNull Player player, @NotNull String item, @NotNull Integer amount) throws IllegalArgumentException {
        Objects.requireNonNull(player, "Player cannot be null");

        ItemStack drop = build(item);
        drop.setAmount(amount);

        if (player.getLocation().getWorld() != null) {
            player.getLocation().getWorld().dropItem(player.getLocation(), drop);
        }
    }

    private static void removeItemsPrecisely(@NotNull Inventory inv, @NotNull ItemStack target, int amount) {
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

    public static void removeItem(@NotNull Inventory playerInventory, @NotNull String ingredient, int amount) {
        if (ingredient.endsWith("*")) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, ingredient);
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
            Material material = Material.getMaterial(ingredient.toUpperCase());
            if (material != null) {
                playerInventory.removeItem(new ItemStack(material, amount));
            } else {
                AdventureUtil.consoleMessage(DebugLevel.WARNING, "Failed to remove unknown ingredient: " + ingredient);
            }
        }
    }

    public static @NotNull ItemStack buildItemAPI(@NotNull String key) throws IllegalArgumentException {
        // Forwarded to build(), which already handles exception throwing safely.
        return build(key);
    }

    /**
     * Checks if a player has the required ingredients.
     * @throws IllegalArgumentException if ingredient amounts are not valid integers.
     */
    public static boolean handleIngredientCheck(@NotNull Inventory playerInventory, @Nullable List<String> ingredients, @NotNull Integer instances) throws IllegalArgumentException {
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

    private static boolean handleOptions(@NotNull Inventory playerInventory, @NotNull String[] options, @NotNull Integer instances) throws IllegalArgumentException {
        for (String option : options) {
            String[] parts = option.split(":");

            if (parts.length < 2) {
                throw new IllegalArgumentException("Malformed ingredient format. Expected 'id:amount', got: " + option);
            }

            int amount;
            try {
                amount = Integer.parseInt(parts[1]) * instances;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid ingredient amount for '" + parts[0] + "': " + parts[1], e);
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
                    Material material = Material.getMaterial(parts[0].toUpperCase());
                    if (material != null && playerInventory.containsAtLeast(new ItemStack(material), amount)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean tieredIngredientCheck(@NotNull Inventory playerInventory, @NotNull String ingredient, @NotNull Integer amount) {
        CustomStack customStack = CustomStack.getInstance(ingredient);
        if (customStack == null) {
            return false;
        }
        for (int tier = 0; tier <= 2; tier++) {
            String tieredIngredient = ingredient + (tier > 0 ? "_t" + tier : "");
            CustomStack customStackTiered = CustomStack.getInstance(tieredIngredient);
            if (customStackTiered != null) {
                ItemStack itemStackTiered = customStackTiered.getItemStack();
                if (playerInventory.containsAtLeast(itemStackTiered, amount)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean fishingIngredientCheck(@NotNull Inventory playerInventory, @NotNull String ingredient, @NotNull Integer amount) {
        if (!ingredient.startsWith("fish")) {
            return false;
        }

        int fishAmount = 0;
        for (ItemStack item : playerInventory.getContents()) {
            if (item == null || item.getType() != Material.COD) continue;

            String customFishingItemID = BukkitCustomFishingPlugin.getInstance().getItemManager().getCustomFishingItemID(item);
            if (customFishingItemID != null) {
                fishAmount += item.getAmount();
                if (fishAmount >= amount) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeIngredients(@NotNull Inventory playerInventory, @Nullable List<String> ingredients, @NotNull Integer instances) throws IllegalArgumentException {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        for (String ingredient : ingredients) {
            String[] options = ingredient.split("/");
            for (String option : options) {
                String[] parts = option.split(":");
                if (parts.length < 2) {
                    throw new IllegalArgumentException("Malformed ingredient format during removal: " + option);
                }

                String ingredientName = parts[0];
                try {
                    int amount = Integer.parseInt(parts[1]) * instances;
                    removeItem(playerInventory, ingredientName, amount);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid amount for ingredient removal: " + parts[1], e);
                }
            }
        }
    }

    public static boolean playerHasIngredient(@NotNull Inventory playerInventory, @NotNull String ingredient) {
        if (ingredient.endsWith("*")) {
            String base = ingredient.replace("*", "");
            if (base.startsWith("fish")) {
                return fishingIngredientCheck(playerInventory, base, 1);
            } else {
                return tieredIngredientCheck(playerInventory, base, 1);
            }
        } else {
            CustomStack customStack = CustomStack.getInstance(ingredient);
            if (customStack != null) {
                return playerInventory.containsAtLeast(customStack.getItemStack(), 1);
            } else {
                Material material = Material.getMaterial(ingredient.toUpperCase());
                return material != null && playerInventory.containsAtLeast(new ItemStack(material), 1);
            }
        }
    }

    static void removeWildcardItem(@NotNull Inventory playerInventory, @NotNull String ingredient, int amount) {
        String baseIngredient = ingredient.replace("*", "");
        if (baseIngredient.startsWith("fish")) {
            int remaining = amount;
            for (int i = 0; i < playerInventory.getSize(); i++) {
                ItemStack item = playerInventory.getItem(i);
                if (item == null || item.getType() != Material.COD) continue;

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
            }
        }
    }

    public static String getDuration(int durationInSeconds) {
        if (durationInSeconds <= 0) return " ";
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        StringBuilder durationString = new StringBuilder().append("<gold>for ");
        if (minutes > 0) durationString.append(minutes).append(minutes > 1 ? " mins " : " min ");
        if (seconds > 0) durationString.append(seconds).append("s");
        return durationString.toString();
    }

    public static String amplifierToRoman(int amplifier) {
        int[] values = {10, 9, 5, 4, 1};
        String[] romanLetters = {"X", "IX", "V", "IV", "I"};
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (amplifier >= values[i]) {
                amplifier -= values[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }
}