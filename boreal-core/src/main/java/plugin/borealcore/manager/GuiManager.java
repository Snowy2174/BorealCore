package plugin.borealcore.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import plugin.borealcore.api.module.BorealGUI;
import plugin.borealcore.functions.cooking.object.Ingredient;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

public class GuiManager extends Function {

    private static final Map<String, Supplier<BorealGUI>> guiRegistry = new HashMap<>();

    public static HashMap<String, Ingredient> INGREDIENTS;
    public static HashMap<String, ItemStack> collectionItems;

    @Override
    public void load() {
        INGREDIENTS = new HashMap<>();
        //collectionItems = initCollectionItems();
        loadItems();
        //writeProgressionItemsToNascraft(collectionItems, new File(BorealCore.getInstance().getDataFolder(), "nascraft.yml"));

        AdventureUtil.consoleMessage("Loaded <green>" + (INGREDIENTS.size()) + " <gray>ingredients");
    }

    @Override
    public void unload() {
        if (INGREDIENTS != null) INGREDIENTS.clear();
        guiRegistry.clear();
    }

    /**
     * Registers a new GUI factory to the core manager.
     * External modules should call this during their onModuleEnable() phase.
     *
     * @param id The unique identifier for this GUI (e.g., "wikiMenu")
     * @param guiSupplier A supplier that creates a new instance of the BorealGUI
     */
    public void registerGui(String id, Supplier<BorealGUI> guiSupplier) {
        guiRegistry.put(id, guiSupplier);
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Registered GUI: " + id);
    }

    /**
     * Unregisters a GUI from the manager.
     * External modules should call this during their onModuleDisable() phase.
     */
    public void unregisterGui(String id) {
        guiRegistry.remove(id);
    }

    /**
     * Opens a registered GUI for a player.
     *
     * @param player The player to open the GUI for
     * @param id The unique identifier of the GUI
     * @return true if the GUI was found and opened, false otherwise
     */
    public static boolean openGui(Player player, String id) {
        Supplier<BorealGUI> supplier = guiRegistry.get(id);
        if (supplier != null) {
            BorealGUI gui = supplier.get();
            gui.open(player); // Native open method from the abstract BorealGUI class
            return true;
        } else {
            AdventureUtil.consoleMessage(DebugLevel.WARNING, "Attempted to open unregistered GUI: " + id);
            return false;
        }
    }

    // Note: Eventually, these should likely be moved to a dedicated IngredientManager / IntegrationManager.

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