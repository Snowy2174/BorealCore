package plugin.customcooking.configs;

import dev.lone.itemsadder.api.ItemsAdder;
import net.luckperms.api.model.user.User;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.minigame.*;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.PermUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeManager extends Function {

    public static HashMap<String, Product> RECIPES;

    public static final Map<String, List<String>> itemIngredients = new HashMap<>();
    public static final Map<String, String> perfectItems = new HashMap<>();
    public static final Map<String, String> successItems = new HashMap<>();
    protected static final Map<String, String> burnedItems = new HashMap<>();
    protected static final Map<String, Integer> masteryreqs = new HashMap<>();
    private static final String PERMISSION_PREFIX = "customcooking.recipe.";


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

    public static void checkAndAddRandomRecipe(Player player) {
        List<String> unlockedRecipes = getUnlockedRecipes(player);
        List<String> lockedRecipes = getLockedRecipes(unlockedRecipes);

        if (lockedRecipes.isEmpty()) {
            // Player has unlocked all recipes
            return;
        }

        String randomRecipe = getRandomRecipe(lockedRecipes);
        addRecipe(player, randomRecipe);
    }

    public static void addRecipe(Player player, String recipe) {
        String permission = "customcooking.recipe." + recipe;
        String recipeFormatted = RECIPES.get(recipe).getNick();

        PermUtil.addPermission(player.getUniqueId(), permission);

        ItemsAdder.playTotemAnimation(player, recipe + "_particle");
        AdventureUtil.playerMessage(player, "<gray>[<green>!<gray>]<green> You have unlocked the recipe " + recipeFormatted);
    }

    public static void removeRecipe(Player player, String recipe) {
        String permission = "customcooking.recipe." + recipe;
        String recipeFormatted = RECIPES.get(recipe).getNick();

        PermUtil.removePermission(player.getUniqueId(), permission);

        ItemsAdder.playTotemAnimation(player, recipe + "_particle");
        AdventureUtil.playerTitle(player, "<red> You have lost the recipe " + recipe, " ", 20, 40, 20);
        AdventureUtil.playerMessage(player, "<gray>[<red>!<gray>]<red> You have lost the recipe " + recipeFormatted);
    }

    private static List<String> getUnlockedRecipes(Player player) {
        return player.getEffectivePermissions().stream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(perm -> perm.startsWith(PERMISSION_PREFIX))
                .map(perm -> perm.substring(PERMISSION_PREFIX.length()))
                .collect(Collectors.toList());
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
