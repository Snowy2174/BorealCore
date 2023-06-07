package plugin.customcooking.gui;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.configs.MasteryManager;
import plugin.customcooking.manager.CookingManager;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;

import static plugin.customcooking.configs.RecipeManager.*;
import static plugin.customcooking.util.InventoryUtil.build;

public class InventoryPopulator implements InventoryProvider {

    private final CookingManager cookingManager;
    private static CustomFurniture clickedFurniture;

    public static SmartInventory getRecipeBook(CustomFurniture clickedFurniture, Player player) {
        return  SmartInventory.builder()
                .manager(CustomCooking.getInventoryManager())
                .id("recipeBook")
                .provider(new InventoryPopulator(clickedFurniture))
                .size(6, 9)
                .title(ChatColor.WHITE + new FontImageWrapper("customcooking:recipe_book").applyPixelsOffset(-16) + ChatColor.RESET + FontImageWrapper.applyPixelsOffsetToString( ChatColor.DARK_AQUA + player.getName() + ChatColor.RESET + "'sRecipe Book", -185))
                .build();
    }
    public InventoryPopulator(CustomFurniture clickedFurniture) {
        this.cookingManager = new CookingManager();
        this.clickedFurniture = clickedFurniture;
    }

    @Override
    public void update(Player player, InventoryContents contents) {}

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fill( ClickableItem.of(build("unknownrecipe"),
                e -> AdventureUtil.playerMessage(player, "<gray>[<red>!<gray>]<red> You haven't unlocked this recipe yet..")));
        contents.fillBorders(ClickableItem.empty(new ItemStack (Material.AIR)));

        List<String> list = getUnlockedRecipes(player);

        for (String recipe : RECIPES.keySet()) {
            boolean hasMastery = MasteryManager.hasMastery(player, recipe);
            boolean hasRecipe = list.contains(recipe);
            ItemStack itemStack;

            if (hasRecipe) {
                if (hasMastery) {
                    itemStack = buildRecipeItem(recipe + "_perfect", player, true);
                } else {
                    itemStack = buildRecipeItem(recipe, player, false);
                }
            } else {
                itemStack = buildUnknownItem(recipe + "_unknown");
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
        if (customStack == null) {
            System.out.println("CustomStack is INVALID! for recipe: " + recipe);
            return new ItemStack(Material.AIR);
        } else {
            ItemStack stack = customStack.getItemStack();
            modifyLore(stack, player, recipe, hasMastery);
            return stack;
        }
    }

    private ItemStack buildUnknownItem(String recipe){
        CustomStack customStack = CustomStack.getInstance(recipe);
        if (customStack == null) {
            return new ItemStack(CustomStack.getInstance("unknownrecipe").getItemStack());
        } else {
            ItemStack stack = customStack.getItemStack();
            ItemMeta itemMeta = stack.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setLore(CustomStack.getInstance("unknownrecipe").getItemStack().getItemMeta().getLore());
                itemMeta.setDisplayName(CustomStack.getInstance("unknownrecipe").getItemStack().getItemMeta().getDisplayName());
                stack.setItemMeta(itemMeta);
            } else {
                System.out.println("ItemMeta is null!");
            }
            return stack;
        }
    }

    private void handleItemClick(InventoryClickEvent event, Player player, String recipe, boolean hasRecipe, boolean hasMastery) {
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            if (hasRecipe) {
                if (event.isLeftClick()) {
                    if (hasMastery) {
                        // Left-click handling logic for autocooking the recipe
                        cookingManager.handleCooking(recipe, player, clickedFurniture, true);
                    } else {
                        // Left-click handling logic for cooking the recipe
                        cookingManager.handleCooking(recipe, player, clickedFurniture, false);
                    }
                } else if (event.isRightClick()) {
                    // Right click handling for cooking the recipe
                    cookingManager.handleCooking(recipe, player, clickedFurniture, false);
                }
                event.setCancelled(true);
            } else {
                AdventureUtil.playerMessage(player, "<gray>[<red>!<gray>]<red> You haven't unlocked this recipe yet..");
            }
        }
    }

    private void modifyLore(ItemStack itemStack, Player player, String recipe, Boolean hasMastery) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
            itemStack.setItemMeta(itemMeta);
        }
        List<String> lore = itemMeta.getLore();
        List<String> ingredients = itemIngredients.get(recipe);

        if (!itemMeta.hasLore()) {
            lore = new ArrayList<>();
            lore.add("This item does not have lore! Configure it correctly in ItemsAdder!");
        }

        appendMastery(lore, player, recipe, hasMastery);
        appendIngredients(lore, player, ingredients);


        lore.add(" ");
        if (hasMastery) {
            lore.add("<!italic><#ffcc33>[Right Click] <#ffcc99>to Cook");
            lore.add("<!italic><#ffcc33>[Left Click] <#ffcc99>to Autocook");
        } else {
            lore.add("<!italic><#ffcc33>[Click] <#ffcc99>to Cook");
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

    private void appendMastery(List<String> lore, Player player, String recipe, Boolean hasMastery) {

        if (hasMastery) {
            lore.add(" ");
            lore.add("<!italic><#ff9900>Mastery [" + MasteryManager.getMasteryCount(player, recipe) + "/" + MasteryManager.getRequiredMastery(recipe) + "]");
            lore.add("<!italic><#ffcc99>This item has been mastered");
            lore.add("<!italic><#ffcc99>and will be cooked automatically.");
        } else {
            lore.add(" ");
            lore.add("<!italic><#ff9900>Mastery [" + MasteryManager.getMasteryCount(player, recipe) + "/" + MasteryManager.getRequiredMastery(recipe) + "]");
            lore.add("<!italic><#ffcc33>[" + appendProgressBar(MasteryManager.getMasteryCount(player, recipe) / MasteryManager.getRequiredMastery(recipe)) + "<#ffcc33>]");
            lore.add("<!italic><#ffcc99>This dish has not been mastered");
            lore.add("<!italic><#ffcc99>and will have to be manually cooked.");
        }
    }

    private String appendProgressBar(double percentage) {
        int length = 10; // Length of the progress bar
        int completedLength = (int) (length * percentage);
        StringBuilder progressBar = new StringBuilder();

        for (int i = 0; i < length; i++) {
            if (i < completedLength) {
                progressBar.append("<#fcac32>■"); // color code for completed blocks
            } else if (i == completedLength) {
                progressBar.append("<#ffcc33>■"); // color code for current value block
            } else {
                progressBar.append("<#ffcc99>■"); // color code for remaining blocks
            }
        }
        return progressBar.toString();
    }

    private void appendIngredients(List<String> lore, Player player, List<String> ingredients) {
        lore.add(" ");
        lore.add("<!italic><#ffcc33>Ingredients:");
        System.out.println(ingredients);

        for (String ingredient : ingredients) {
            String[] parts = ingredient.split(":");

            if (parts[0].endsWith("*")) {
                parts[0].replaceAll("\\*", "");
            }
            String ingredientFormatted = formatString(parts[0]);

            if (InventoryUtil.playerHasIngredient(player.getInventory(), parts[0])) {
                lore.add(ChatColor.GREEN + "- (x" + parts[1] + ") " + ingredientFormatted );
            } else {
                lore.add(ChatColor.RED + "- (x" + parts[1] + ") " + ingredientFormatted );
            }
        }
    }

    private String formatString(String input) {
        String[] words = input.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                result.append(capitalizedWord).append(" ");
            }
        }

        return result.toString().trim();
    }

}
