package plugin.customcooking.configs;

import dev.lone.itemsadder.api.ItemsAdder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.minigame.*;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static plugin.customcooking.configs.MasteryManager.*;
import static plugin.customcooking.util.RecipeDataUtil.removeRecipeMasteryData;
import static plugin.customcooking.util.RecipeDataUtil.setRecipeData;

public class RecipeManager extends Function {

    public static HashMap<String, Product> RECIPES;

    public static final Map<String, List<String>> itemIngredients = new HashMap<>();
    public static final Map<String, String> perfectItems = new HashMap<>();
    public static final Map<String, String> successItems = new HashMap<>();
    protected static final Map<String, String> burnedItems = new HashMap<>();
    protected static final Map<String, Integer> masteryreqs = new HashMap<>();


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
            Set<String> recipes = config.getKeys(false);

            for (String key : recipes) {

                ConfigurationSection recipeSection = config.getConfigurationSection(key);

                // Gui settings


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
                recipe.setNick(recipeSection.getString("nick", key));
                // Set slot
                recipe.setSlot(recipeSection.getInt("slot"));
                // Set layout
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

                // Check if any errors occurred during recipe loading
                if (difficulties.isEmpty()) {
                    AdventureUtil.consoleMessage("<red>[CustomCooking] Recipe '" + key + "' doesn't have valid difficulty");
                    continue;
                }

                if (ingredientStrings.isEmpty()) {
                    AdventureUtil.consoleMessage("<red>[CustomCooking] Recipe '" + key + "' doesn't have any ingredients");
                    continue;
                }

                if (perfectItemString == null || perfectItemString.isEmpty()) {
                    AdventureUtil.consoleMessage("<red>[CustomCooking] Recipe '" + key + "' doesn't have perfectItems defined");
                    continue;
                }

                if (cookedItemString == null || cookedItemString.isEmpty()) {
                    AdventureUtil.consoleMessage("<red>[CustomCooking] Recipe '" + key + "' doesn't have cookedItems defined");
                    continue;
                }

                if (burnedItemString == null || burnedItemString.isEmpty()) {
                    AdventureUtil.consoleMessage("<red>[CustomCooking] Recipe '" + key + "' doesn't have burnedItems defined");
                    continue;
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


    public static void checkAndAddRandomRecipe(Player player) {
        List<String> unlockedRecipes = getUnlockedRecipes(player);
        List<String> lockedRecipes = getLockedRecipes(unlockedRecipes);

        if (lockedRecipes.isEmpty()) {
            // Player has unlocked all recipes
            return;
        }

        String randomRecipe = getRandomRecipe(lockedRecipes);
        unlockRecipe(player, randomRecipe);
    }

    public static void unlockAllRecipes(Player player) {
        List<String> unlockedRecipes = getUnlockedRecipes(player);
        List<String> lockedRecipes = getLockedRecipes(unlockedRecipes);

        for (String recipe : lockedRecipes) {
            unlockRecipe(player, recipe);
        }
    }


    public static void unlockRecipe(Player player, String recipe) {
        String recipeFormatted = RECIPES.get(recipe).getNick();
        setRecipeData(player, recipe, 0);

        ItemsAdder.playTotemAnimation(player, recipe + "_particle");
        AdventureUtil.playerMessage(player, "<gray>[<green>!<gray>]<green> You have unlocked the recipe " + recipeFormatted);
    }

    public static void lockRecipe(Player player, String recipe) {
        String recipeFormatted = RECIPES.get(recipe).getNick();

        removeRecipeMasteryData(player, recipe);

        ItemsAdder.playTotemAnimation(player, recipe + "_particle");
        AdventureUtil.playerMessage(player, "<gray>[<red>!<gray>]<red> You have lost the recipe " + recipeFormatted);
    }

    public static List<String> getUnlockedRecipes(Player player) {
        YamlConfiguration config = ConfigUtil.getConfig("playerdata.yml");
        String playerName = player.getName();

        if (!config.contains("players." + playerName)) {
            return Collections.emptyList();
        }

        ConfigurationSection playerSection = config.getConfigurationSection("players." + playerName);
        if (playerSection != null) {
            return new ArrayList<>(playerSection.getKeys(false));
        }
        return Collections.emptyList();
    }


    private static List<String> getLockedRecipes(List<String> unlockedRecipes) {
        return RECIPES.keySet().stream()
                .filter(recipe -> !unlockedRecipes.contains(recipe))
                .collect(Collectors.toList());
    }

    private static String getRandomRecipe(List<String> recipes) {
        Random random = new Random();
        int index = random.nextInt(recipes.size());
        return recipes.get(index);
    }
}
