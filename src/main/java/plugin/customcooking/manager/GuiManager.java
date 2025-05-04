package plugin.customcooking.manager;

import de.tr7zw.nbtapi.NBT;
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
import plugin.customcooking.CustomCooking;
import plugin.customcooking.functions.collections.CollectionTrackerProvider;
import plugin.customcooking.functions.cooking.IngredientBookProvider;
import plugin.customcooking.functions.cooking.RecipeBookProvider;
import plugin.customcooking.functions.cooking.object.Ingredient;
import plugin.customcooking.functions.wiki.WikiGuiProvider;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;
import plugin.customcooking.utility.ConfigUtil;
import plugin.customcooking.utility.InventoryUtil;

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

    public static SmartInventory getRecipeBook(CustomFurniture clickedFurniture) {
        return SmartInventory.builder()
                .manager(CustomCooking.getInventoryManager())
                .id("recipeBook")
                .provider(new RecipeBookProvider(clickedFurniture))
                .size(6, 9)
                .title(ChatColor.WHITE + new FontImageWrapper(ConfigManager.recipeBookTextureNamespace).applyPixelsOffset(-16) + ChatColor.RESET + FontImageWrapper.applyPixelsOffsetToString(ChatColor.RESET + "Recipe Book", -190))
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
            Double price = NBT.get(itemStack, nbt -> nbt.getDouble("Price"));
            System.out.println("Price: " + price);

            Map<String, Object> entryData = new HashMap<>();
            entryData.put("initial-price", price);
            yamlData.put(key, entryData);
        }
        try (FileWriter writer = new FileWriter(outputFile)) {
            yaml.dump(yamlData, writer);
            System.out.println("Written file: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        INGREDIENTS = new HashMap<>();
        collectionItems = initCollectionItems();
        INGREDIENTS_MENU = getIngredientsBook();
        PROGRESSION_MENU = getProgressionTracker();
        WIKI_MENU = getWikiMenu();
        loadItems();
        //writeProgressionItemsToNascraft(collectionItems, new File(CustomCooking.getInstance().getDataFolder(), "nascraft.yml"));
        AdventureUtil.consoleMessage("[CustomCooking] Loaded <green>" + (INGREDIENTS.size()) + " <gray>ingredients");
        AdventureUtil.consoleMessage("[CustomCooking] Loaded <green>" + (collectionItems.size()) + " <gray>progression items");
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
