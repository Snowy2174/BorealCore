package plugin.customcooking.Configs;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.Minigame.*;
import plugin.customcooking.Util.AdventureUtil;

import java.io.File;
import java.util.*;

public class RecipeManager extends Function {

    public static HashMap<String, Product> RECIPES;

    public static final Map<String, List<String>> itemIngredients = new HashMap<>();
    public static final Map<String, String> perfectItems = new HashMap<>();
    public static final Map<String, String> successItems = new HashMap<>();
    public static final Map<String, String> burnedItems = new HashMap<>();
    public static final Map<String, Integer> masteryreqs = new HashMap<>();


    @Override
    public void load() {
        RECIPES = new HashMap<>();
        loadItems();
        AdventureUtil.consoleMessage("[CustomCooking] Loaded <green>" + (RECIPES.size()) + " <gray>recipes");
    }

    @Override
    public void unload() {
        if (RECIPES != null) RECIPES.clear();
    }

    private void loadItems() {
        File recipe_file = new File(CustomCooking.plugin.getDataFolder() + File.separator + "recipes");
        if (!recipe_file.exists()) {
            if (!recipe_file.mkdir()) return;
            CustomCooking.plugin.saveResource(CustomCooking.plugin.getDataFolder() + File.separator + "recipes.yml", false);
        }
        File[] files = recipe_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);

            for (String key : keys) {

                ConfigurationSection recipeSection = config.getConfigurationSection(key);

                // Bar mechanic
                List<Difficulty> difficulties = new ArrayList<>();
                List<String> difficultyList = recipeSection.getStringList("difficulty");
                if (difficultyList.size() == 0) {
                    String[] diff = StringUtils.split(recipeSection.getString("difficulty", "1-1"), "-");
                    Difficulty difficulty = new Difficulty(Integer.parseInt(diff[0]), Integer.parseInt(diff[1]));
                    difficulties.add(difficulty);
                } else {
                    for (String difficultyStr : difficultyList) {
                        String[] diff = StringUtils.split(difficultyStr, "-");
                        Difficulty difficulty = new Difficulty(Integer.parseInt(diff[0]), Integer.parseInt(diff[1]));
                        difficulties.add(difficulty);
                    }
                }
                int time = recipeSection.getInt("time", 10000);
                int mastery = recipeSection.getInt("mastery", 10);
                DroppedItem recipe = new DroppedItem(
                        key,
                        difficulties.toArray(new Difficulty[0]),
                        time
                );
                // Input Outputs
                List<String> ingredientStrings = recipeSection.getStringList("ingredients");
                String perfectItemString = recipeSection.getString("perfectItems");
                String cookedItemString = recipeSection.getString("cookedItems");
                String burnedItemString = recipeSection.getString("burnedItems");

                // Set nick
                recipe.setNick(recipeSection.getString("nick"));

                if (recipeSection.contains("layout")) {
                    List<Layout> layoutList = new ArrayList<>();
                    for (String layoutName : recipeSection.getStringList( "layout")) {
                        Layout layout = LayoutManager.LAYOUTS.get(layoutName);
                        if (layout == null) {
                            AdventureUtil.consoleMessage("<red>[CustomCooking] Bar " + layoutName + " doesn't exist");
                            continue;
                        }
                        layoutList.add(layout);
                    }
                    recipe.setLayout(layoutList.toArray(new Layout[0]));
                }


                RECIPES.put(key, recipe);

                // store the list of required ingredients for this item
                itemIngredients.put(key, ingredientStrings);
                // store the output options
                perfectItems.put(key, perfectItemString);
                successItems.put(key, cookedItemString);
                burnedItems.put(key, burnedItemString);
                // stores the mastery requirements
                masteryreqs.put(key, mastery);



            }
        }
    }
}
