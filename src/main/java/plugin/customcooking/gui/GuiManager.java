package plugin.customcooking.gui;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.wiki.WikiGuiProvider;

import java.util.*;

import static dev.lone.itemsadder.api.ItemsAdder.getAllItems;
import static plugin.customcooking.util.ConfigUtil.getConfig;
import static plugin.customcooking.util.InventoryUtil.build;

public class GuiManager extends Function {

    public static HashMap<String, Ingredient> INGREDIENTS;
    public static SmartInventory INGREDIENTS_MENU;
    public static SmartInventory PROGRESSION_MENU;
    public static SmartInventory WIKI_MENU;
    public static List<ItemStack> collectionItems;

    @Override
    public void load() {
        INGREDIENTS = new HashMap<>();
        collectionItems = initCollectionItems();
        INGREDIENTS_MENU = getIngredientsBook();
        PROGRESSION_MENU = getProgressionTracker();
        WIKI_MENU = getWikiMenu();
        loadItems();
        AdventureUtil.consoleMessage("[CustomCooking] Loaded <green>" + (INGREDIENTS.size()) + " <gray>ingredients");
        AdventureUtil.consoleMessage("[CustomCooking] Loaded <green>" + (collectionItems.size()) + " <gray>progression items");
    }

    @Override
    public void unload() {
        if (INGREDIENTS != null) INGREDIENTS.clear();
    }

    private static List<ItemStack> initCollectionItems() {
        Set<String> list = CustomStack.getNamespacedIdsInRegistry();
        System.out.println(list.size());
        List<ItemStack> itemStacks = new ArrayList<>();
        for (String str : list) {
            if ((str.startsWith("customcrops:") || str.startsWith("customcooking:") || str.startsWith("customfishing:")) &&
                    !(str.endsWith("_t1") || str.endsWith("_t2"))) {
                itemStacks.add(build(str));
            }
        }

        return itemStacks;
    }

    public static SmartInventory getRecipeBook(CustomFurniture clickedFurniture) {
        return  SmartInventory.builder()
                .manager(CustomCooking.getInventoryManager())
                .id("recipeBook")
                .provider(new RecipeBookProvider(clickedFurniture))
                .size(6, 9)
                .title(ChatColor.WHITE + new FontImageWrapper(ConfigManager.recipeBookTextureNamespace).applyPixelsOffset(-16) + ChatColor.RESET + FontImageWrapper.applyPixelsOffsetToString( ChatColor.RESET + "Recipe Book", -190))
                .build();
    }

    public static SmartInventory getWikiMenu() {
        return SmartInventory.builder()
                .manager(CustomCooking.getInventoryManager())
                .id("wikiMenu")
                .provider(new WikiGuiProvider())
                .size(6, 9)
                .title(ChatColor.WHITE + "Wiki Menu")
                .build();
    }

    public static SmartInventory getIngredientsBook() {
        return SmartInventory.builder()
                .manager(CustomCooking.getInventoryManager())
                .id("ingredientsMenu")
                .provider(new IngredientBookProvider())
                .size(6, 9)
                .title(ChatColor.WHITE + "Ingredients Menu")
                .build();
    }

    public static SmartInventory getProgressionTracker() {
        return SmartInventory.builder()
                .manager(CustomCooking.getInventoryManager())
                .id("progressionTracker")
                .provider(new CollectionTrackerProvider())
                .size(6, 9)
                .title(ChatColor.WHITE + "Player Progression")
                .build();
    }


    private void loadItems() {
            YamlConfiguration config = getConfig("ingredients.yml");
            Set<String> ingredients = config.getKeys(false);

            for (String key : ingredients) {

                ConfigurationSection ingredientSection = config.getConfigurationSection(key);

                Ingredient ingredient = new Ingredient(
                        key,
                        ingredientSection.getString("nick", key),
                        ingredientSection.getInt("slot", 1),
                        ingredientSection.getStringList("ingredients")
                );

                INGREDIENTS.put(key, ingredient);
            }
        }
    }
