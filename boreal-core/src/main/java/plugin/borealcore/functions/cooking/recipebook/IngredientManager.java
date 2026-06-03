package plugin.borealcore.functions.cooking.recipebook;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import plugin.borealcore.functions.cooking.object.Ingredient;
import plugin.borealcore.manager.ConfigManager;
import plugin.borealcore.object.DebugLevel;
import plugin.borealcore.utility.AdventureUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class IngredientManager {

    public static HashMap<String, Ingredient> INGREDIENTS = new HashMap<>();

    private void loadItems() {
        YamlConfiguration config = ConfigManager.getConfig("recipes/ingredients.yml");
        if (config == null) return;

        Set<String> ingredients = config.getKeys(false);

        for (String key : ingredients) {
            ConfigurationSection ingredientSection = config.getConfigurationSection(key);
            if (ingredientSection == null) continue;

            Ingredient ingredient = new Ingredient(
                    key,
                    ingredientSection.getString("nick", key),
                    ingredientSection.getInt("slot", 1),
                    ingredientSection.getStringList("ingredients")
            );

            INGREDIENTS.put(key, ingredient);
        }
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
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Price: " + price);

            Map<String, Object> entryData = new HashMap<>();
            entryData.put("initial-price", price);
            yamlData.put(key, entryData);
        }

        try (FileWriter writer = new FileWriter(outputFile)) {
            yaml.dump(yamlData, writer);
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Written file: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
