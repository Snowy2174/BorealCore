package plugin.borealcore.functions.cooking;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.borealcore.BorealCore;
import plugin.borealcore.api.module.BorealGUI;
import plugin.borealcore.functions.cooking.configs.CookingConfig;
import plugin.borealcore.functions.cooking.configs.CookingMessage;
import plugin.borealcore.functions.cooking.configs.RecipeManager;
import plugin.borealcore.object.DebugLevel;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.GuiUtil;
import plugin.borealcore.utility.ItemUtil;

import java.util.ArrayList;
import java.util.List;

import static plugin.borealcore.utility.ItemUtil.build;

public class CookingRecipeBookGUI extends BorealGUI {

    private final CustomFurniture clickedFurniture;
    private static ItemStack unknownRecipeStack;
    private final CookingManager cookingManager;

    public CookingRecipeBookGUI(CustomFurniture clickedFurniture) {
        super(6, Component.text(ChatColor.WHITE + new FontImageWrapper(CookingConfig.recipeBookTextureNamespace).applyPixelsOffset(-16) + ChatColor.RESET + FontImageWrapper.applyPixelsOffsetToString(ChatColor.RESET + "Recipe Book", -190)));

        this.cookingManager = BorealCore.getCookingManager();
        this.clickedFurniture = clickedFurniture;

        if (unknownRecipeStack == null) {
            unknownRecipeStack = build(CookingConfig.unknownItem);
        }
    }

    @Override
    public void init(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
        fill(unknownRecipeStack, e -> AdventureUtil.playerMessage(player, MessageManager.infoNegative + CookingMessage.recipeUnknown));
        fillBorders(new ItemStack(Material.AIR), null);

        setItem(5, 4, buildIngredientsItem(), e -> handleIngredientsMenuClick(e, player));

        List<String> unlockedRecipes = RecipeDataUtil.getUnlockedRecipes(player);

        for (String recipe : RecipeManager.COOKING_RECIPES.keySet()) {
            boolean hasMastery = RecipeDataUtil.hasMastery(player, recipe);
            boolean hasRecipe = unlockedRecipes.contains(recipe);
            ItemStack itemStack;

            if (hasRecipe) {
                itemStack = buildRecipeItem(recipe, player, hasMastery);
            } else {
                itemStack = buildUnknownRecipeItem(recipe);
            }

            int slot = RecipeManager.COOKING_RECIPES.get(recipe).getSlot(); // Retrieve the slot from the configuration
            if (slot != -1) {
                int row = (slot - 1) / 9; // Calculate the row based on the slot
                int column = (slot - 1) % 9;  // Calculate the column based on the slot

                setItem(row, column, itemStack, e -> handleItemClick(e, player, recipe, hasRecipe, hasMastery));
            }
        }
    }

    private ItemStack buildRecipeItem(String recipe, Player player, boolean hasMastery) {
        CustomStack customStack = CustomStack.getInstance(recipe);
        if (hasMastery) {
            customStack = CustomStack.getInstance(recipe + CookingConfig.perfectItemSuffix);
        }
        if (customStack == null) {
            return unknownRecipeStack;
        } else {
            ItemStack stack = customStack.getItemStack();
            ItemUtil.addPotionEffectLore(stack, recipe, false);
            modifyLore(stack, player, recipe, hasMastery);
            return stack;
        }
    }

    private ItemStack buildUnknownRecipeItem(String recipe) {
        CustomStack customStack = CustomStack.getInstance(recipe + CookingConfig.unknownItemSuffix);
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

    private ItemStack buildIngredientsItem() {
        return new ItemStack(CustomStack.getInstance(CookingConfig.grinderItem).getItemStack());
    }

    private void handleIngredientsMenuClick(InventoryClickEvent event, Player player) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            String command = "dm open ingredients_menu " + player.getName();
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    private void modifyLore(ItemStack itemStack, Player player, String recipe, Boolean hasMastery) {
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

        GuiUtil.appendMastery(lore, player, recipe, hasMastery);

        if (RecipeManager.COOKING_RECIPES.get(recipe).getIngredients() != null) {
            GuiUtil.appendIngredients(lore, player, RecipeManager.COOKING_RECIPES.get(recipe).getIngredients());
        }

        if (clickedFurniture != null) {
            lore.add(" ");
            if (Boolean.TRUE.equals(hasMastery)) {
                lore.add(CookingConfig.cookLineRight);
                lore.add(CookingConfig.cookLineLeft);
                lore.add(CookingConfig.cookLineShift);
            } else {
                lore.add(CookingConfig.cookLine);
            }
        }

        List<Component> parsedLore = new ArrayList<>();

        for (String line : lore) {
            parsedLore.add(AdventureUtil.getComponentFromMiniMessage(line));
        }

        itemMeta.lore(parsedLore);
        itemStack.setItemMeta(itemMeta);
    }

    private void handleItemClick(InventoryClickEvent event, Player player, String recipe, boolean hasRecipe, boolean hasMastery) {
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            if (clickedFurniture == null) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + CookingMessage.recipeNoPot);
                return;
            }

            if (!hasRecipe) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + CookingMessage.recipeUnknown);
                return;
            }

            if (hasMastery) {
                if (event.getClick() == ClickType.MIDDLE) {
                    cookingManager.handleAutocooking(recipe, player, 16);
                } else if (event.isRightClick()) {
                    cookingManager.handleCooking(recipe, player, clickedFurniture);
                } else if (event.isLeftClick()) {
                    cookingManager.handleAutocooking(recipe, player, 1);
                }
            } else {
                cookingManager.handleCooking(recipe, player, clickedFurniture);
            }

            // Event is cancelled automatically by BorealGUI, but you can leave this here safely
            event.setCancelled(true);
        }
    }
}