package plugin.borealcore.manager;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.brewery.BreweryRecipeBookProvider;
import plugin.borealcore.functions.collections.CollectionTrackerProvider;
import plugin.borealcore.functions.cooking.CookingRecipeBookProvider;
import plugin.borealcore.functions.cooking.IngredientBookProvider;
import plugin.borealcore.functions.cooking.object.Ingredient;
import plugin.borealcore.functions.wiki.WikiGuiProvider;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.ConfigUtil;
import plugin.borealcore.utility.InventoryUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GuiManager extends Function {

    public static HashMap<String, Ingredient> INGREDIENTS;
    public static SmartInventory INGREDIENTS_MENU;
    public static SmartInventory PROGRESSION_MENU;
    public static SmartInventory WIKI_MENU;
    public static HashMap<String, ItemStack> collectionItems;

    @Override
    public void load() {
        INGREDIENTS = new HashMap<>();
        //collectionItems = initCollectionItems();
        INGREDIENTS_MENU = getIngredientsBook();
        //PROGRESSION_MENU = getProgressionTracker();
        WIKI_MENU = getWikiMenu();
        loadItems();
        //writeProgressionItemsToNascraft(collectionItems, new File(BorealCore.getInstance().getDataFolder(), "nascraft.yml"));
        AdventureUtil.consoleMessage("Loaded <green>" + (INGREDIENTS.size()) + " <gray>ingredients");
        //AdventureUtil.consoleMessage("Loaded <green>" + (collectionItems.size()) + " <gray>progression items");
    }

    private static HashMap<String, ItemStack> initCollectionItems() {
        Set<String> list = CustomStack.getNamespacedIdsInRegistry();
        HashMap<String, ItemStack> itemStacks = new HashMap<>();
        for (String str : list) {
            if ((str.startsWith("customcrops:") || str.startsWith("customcooking:") || str.startsWith("customfishing:")) &&
                    !((str.contains("stage") || str.contains("unknown") || str.contains("particle")))) {
                itemStacks.put(str, InventoryUtil.build(str));
            }
        }
        return itemStacks;
    }

    public static SmartInventory getCookingRecipeBook(CustomFurniture clickedFurniture) {
        return SmartInventory.builder()
                .manager(BorealCore.getInventoryManager())
                .id("recipeBook")
                .provider(new CookingRecipeBookProvider(clickedFurniture))
                .size(6, 9)
                .title(ChatColor.WHITE + new FontImageWrapper(ConfigManager.recipeBookTextureNamespace).applyPixelsOffset(-16) + ChatColor.RESET + FontImageWrapper.applyPixelsOffsetToString(ChatColor.RESET + "Recipe Book", -190))
                .build();
    }

    public static SmartInventory getBrewingRecipeBook() {
        return SmartInventory.builder()
                .manager(BorealCore.getInventoryManager())
                .id("brewBook")
                .provider(new BreweryRecipeBookProvider())
                .size(6, 9)
                .title(ChatColor.WHITE + new FontImageWrapper(ConfigManager.recipeBookTextureNamespace).applyPixelsOffset(-16) + ChatColor.RESET + FontImageWrapper.applyPixelsOffsetToString(ChatColor.RESET + "Brewing Book", -190))
                .build();
    }

    public static SmartInventory getWikiMenu() {
        return SmartInventory.builder()
                .manager(BorealCore.getInventoryManager())
                .id("wikiMenu")
                .provider(new WikiGuiProvider())
                .size(6, 9)
                .title(ChatColor.WHITE + "Wiki Menu")
                .build();
    }

    public static SmartInventory getIngredientsBook() {
        return SmartInventory.builder()
                .manager(BorealCore.getInventoryManager())
                .id("ingredientsMenu")
                .provider(new IngredientBookProvider())
                .size(6, 9)
                .title(ChatColor.WHITE + "Ingredients Menu")
                .build();
    }

    public static SmartInventory getProgressionTracker() {
        return SmartInventory.builder()
                .manager(BorealCore.getInventoryManager())
                .id("progressionTracker")
                .provider(new CollectionTrackerProvider())
                .size(6, 9)
                .title(ChatColor.WHITE + "Player Progression")
                .build();
    }

    public static void writeProgressionItemsToNascraft(HashMap<String, ItemStack> map, File outputFile) {
        // Configure YAML options
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);

        Map<String, ItemStack> sortedMap = new TreeMap<>((key1, key2) -> {
            String[] parts1 = key1.split(":");
            String[] parts2 = key2.split(":");
            int namespaceComparison = parts1[0].compareTo(parts2[0]);
            if (namespaceComparison != 0) {
                return namespaceComparison;
            }
            return parts1[1].compareTo(parts2[1]);
        });
        sortedMap.putAll(map);

        Map<String, Object> yamlData = new LinkedHashMap<>();

        for (Map.Entry<String, ItemStack> entry : sortedMap.entrySet()) {
            String key = entry.getKey().split(":")[1];
            ItemStack itemStack = entry.getValue();
            Double price = 0.0;
            AdventureUtil.consoleMessage(DebugLevel.DEBUG,"Price: " + price);

            Map<String, Object> entryData = new HashMap<>();
            entryData.put("initial-price", price);
            yamlData.put(key, entryData);
        }
        try (FileWriter writer = new FileWriter(outputFile)) {
            yaml.dump(yamlData, writer);
            AdventureUtil.consoleMessage(DebugLevel.DEBUG,"Written file: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unload() {
        if (INGREDIENTS != null) INGREDIENTS.clear();
    }

    private void loadItems() {
        YamlConfiguration config = ConfigUtil.getConfig("recipes/ingredients.yml");
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
