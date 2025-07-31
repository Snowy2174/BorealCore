package plugin.borealcore.functions.cooking.configs;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.Difficulty;
import plugin.borealcore.functions.cooking.object.DroppedItem;
import plugin.borealcore.functions.cooking.object.Layout;
import plugin.borealcore.functions.cooking.object.Recipe;
import plugin.borealcore.manager.EffectManager;
import plugin.borealcore.manager.configs.DebugLevel;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class RecipeManager extends Function {

    public static HashMap<String, Recipe> COOKING_RECIPES;

    @Override
    public void load() {
        COOKING_RECIPES = new HashMap<>();
        loadItems();
        AdventureUtil.consoleMessage("Loaded <green>" + (COOKING_RECIPES.size()) + " <gray>dish recipes");
    }

    @Override
    public void unload() {
        if (COOKING_RECIPES != null) COOKING_RECIPES.clear();
    }

    private void loadItems() {
        File recipe_file = new File(BorealCore.plugin.getDataFolder() + File.separator + "recipes");
        if (!recipe_file.exists()) {
            if (!recipe_file.mkdir()) return;
            BorealCore.plugin.saveResource(BorealCore.plugin.getDataFolder() + File.separator + "recipes.yml", false);
        }
        File[] files = recipe_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().equals("recipes.yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> recipes = config.getKeys(false);

            for (String key : recipes) {

                ConfigurationSection recipeSection = config.getConfigurationSection(key);

                // Bar mechanic
                List<Difficulty> difficulties = new ArrayList<>();
                List<String> difficultyList = recipeSection.getStringList("difficulty");
                if (difficultyList.isEmpty()) {
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
                DroppedItem recipe = new DroppedItem(
                        key,
                        recipeSection.getString("nick", key),
                        difficulties.toArray(new Difficulty[0]),
                        recipeSection.getStringList("ingredients"),
                        recipeSection.getString("cookedItems"),
                        recipeSection.getInt("time", 10000),
                        recipeSection.getInt("mastery", 10),
                        recipeSection.getInt("slot", 1),
                        recipeSection.getDouble("score", 1)
                );

                // Set layout
                if (recipeSection.contains("layout")) {
                    List<Layout> layoutList = new ArrayList<>();
                    for (String layoutName : recipeSection.getStringList("layout")) {
                        Layout layout = LayoutManager.LAYOUTS.get(layoutName);
                        if (layout == null) {
                            AdventureUtil.consoleMessage(DebugLevel.ERROR, "Bar" + layoutName + " doesn't exist");
                            continue;
                        }
                        layoutList.add(layout);
                    }
                    recipe.setLayout(layoutList.toArray(new Layout[0]));
                }

                setActions(recipeSection, recipe);

                recipe.setDishEffectsLore(EffectManager.buildActionsLore(recipe.getConsumeActions()));

                COOKING_RECIPES.put(key, recipe);
            }
        }
    }

    private void setActions(ConfigurationSection section, Recipe recipe) {
        recipe.setSuccessActions(EffectManager.getActions(section.getConfigurationSection("action.success"), recipe.getNick(), false));
        recipe.setFailureActions(EffectManager.getActions(section.getConfigurationSection("action.failure"), recipe.getNick(), false));
        recipe.setConsumeActions(List.of(EffectManager.getActions(section.getConfigurationSection("action.consume"), recipe.getNick(), false), EffectManager.getActions(section.getConfigurationSection("action.consume"), recipe.getNick(), true)));
    }

}
