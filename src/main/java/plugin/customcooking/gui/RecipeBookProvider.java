package plugin.customcooking.gui;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.customcooking.manager.CookingManager;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.GUIUtil;
import plugin.customcooking.util.RecipeDataUtil;

import java.util.ArrayList;
import java.util.List;

import static plugin.customcooking.manager.configs.RecipeManager.RECIPES;
import static plugin.customcooking.util.InventoryUtil.build;

public class RecipeBookProvider implements InventoryProvider {
    private final CookingManager cookingManager;
    private static CustomFurniture clickedFurniture;
    private static ItemStack unknownRecipeStack;
    RecipeBookProvider(CustomFurniture clickedFurniture) {
        this.cookingManager = new CookingManager();
        this.clickedFurniture = clickedFurniture;
        this.unknownRecipeStack = build(ConfigManager.unknownItem);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        // Doesn't do anything yet, eventually will update ingredients?
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
        contents.fill( ClickableItem.of(unknownRecipeStack,
                e -> AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.recipeUnknown)));
        contents.fillBorders(ClickableItem.empty(new ItemStack(Material.AIR)));
        contents.set(5, 4, ClickableItem.of(buildIngredientsItem(), e -> handleIngredientsMenuClick(e, player)));

        List<String> list = RecipeDataUtil.getUnlockedRecipes(player);

        for (String recipe : RECIPES.keySet()) {
            boolean hasMastery = RecipeDataUtil.hasMastery(player, recipe);
            boolean hasRecipe = list.contains(recipe);
            ItemStack itemStack;

            if (hasRecipe) {
                itemStack = buildRecipeItem(recipe, player, hasMastery);
            } else {
                itemStack = buildUnknownRecipeItem(recipe);
            }

            int slot = RECIPES.get(recipe).getSlot(); // Retrieve the slot from the configuration
            if (slot != -1) {
                int row = (slot - 1) / 9; // Calculate the row based on the slot
                int column = (slot - 1) % 9;  // Calculate the column based on the slot

                contents.set(row, column, ClickableItem.of(itemStack, e -> handleItemClick(e, player, recipe, hasRecipe, hasMastery)));
            }
        }
    }

    private ItemStack buildRecipeItem(String recipe, Player player, boolean hasMastery){
        CustomStack customStack = CustomStack.getInstance(recipe);
        if (hasMastery) {customStack = CustomStack.getInstance(recipe+ ConfigManager.perfectItemSuffix);}
        if (customStack == null) {
            return unknownRecipeStack;
        } else {
            ItemStack stack = customStack.getItemStack();
            modifyLore(stack, player, recipe, hasMastery);
            return stack;
        }
    }

    private ItemStack buildUnknownRecipeItem(String recipe){
        CustomStack customStack = CustomStack.getInstance(recipe+ConfigManager.unknownItemSuffix);
        if (customStack == null) {
            return unknownRecipeStack;
        } else {
            ItemStack stack = customStack.getItemStack();
            ItemMeta itemMeta = stack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setLore(unknownRecipeStack.getItemMeta().getLore());
                stack.setItemMeta(itemMeta);
            } else {
                System.out.println("ItemMeta is null!");
            }
            return stack;
        }
    }

    private ItemStack buildIngredientsItem(){
        return new ItemStack(CustomStack.getInstance(ConfigManager.grinderItem).getItemStack());
    }
    private void handleIngredientsMenuClick(InventoryClickEvent event, Player player) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            GuiManager.INGREDIENTS_MENU.open(player);
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

        GUIUtil.appendMastery(lore, player, recipe, hasMastery);

        if (RECIPES.get(recipe).getIngredients() != null) {
            GUIUtil.appendIngredients(lore, player, RECIPES.get(recipe).getIngredients());
        }


        if (clickedFurniture != null) {
            lore.add(" ");
            if (Boolean.TRUE.equals(hasMastery)) {
                lore.add(ConfigManager.cookLineRight);
                lore.add(ConfigManager.cookLineLeft);
                lore.add(ConfigManager.cookLineShift);
            } else {
                lore.add(ConfigManager.cookLine);
            }
        }

        // Create a new list to store parsed lore
        List<Component> parsedLore = new ArrayList<>();

        // Parse each lore line and add it to the parsedLore list
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
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.recipeNoPot);
                return;
            }

            if (!hasRecipe) {
                AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.recipeUnknown);
                return;
            }
            if (hasMastery) {
                if (event.isShiftClick()) {
                    // Shift click handling for cooking 15 Recipes
                    cookingManager.handleAutocooking(recipe, player, 15);
                } else if (event.isRightClick()) {
                    // Right click handling for cooking the recipe
                    cookingManager.handleCooking(recipe, player, clickedFurniture);
                } else if (event.isLeftClick()) {
                    // Left-click handling logic for autocooking the recipe
                    cookingManager.handleAutocooking(recipe, player, 1);
                }
            } else {
                cookingManager.handleCooking(recipe, player, clickedFurniture);
            }
            event.setCancelled(true);
        }
    }


}
