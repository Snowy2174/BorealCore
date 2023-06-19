package plugin.customcooking.util;

import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.manager.configs.MessageManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static plugin.customcooking.manager.configs.RecipeManager.RECIPES;
import static plugin.customcooking.util.ConfigUtil.getConfig;

public class RecipeDataUtil {

    public static int getMasteryCount(Player player, String recipe) {
        YamlConfiguration config = getConfig("playerdata.yml");
        int count = config.getInt("players." + player.getName() + "." + recipe, 0);
        return count;
    }

    public static int getDefaultRequiredMastery(String recipe) {
        Integer mastery = RECIPES.get(recipe).getMasteryreq();
        if (mastery == null) {
            return 10;
        }
        return mastery;
    }

    public static boolean hasMastery(Player player, String recipe) {
        int count = getMasteryCount(player, recipe);
        int requiredMastery = getDefaultRequiredMastery(recipe);
        return count >= requiredMastery;
    }

    private static void updateRecipeStatus(Player player, String recipe, boolean unlock) {
        String recipeFormatted = RECIPES.get(recipe).getNick();

        if (unlock) {
            ItemsAdder.playTotemAnimation(player, recipe + "_particle");
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.recipeUnlocked.replace("{recipe}", recipeFormatted));
        } else {
            ItemsAdder.playTotemAnimation(player, recipe + "_particle");
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.recipeLocked.replace("{recipe}", recipeFormatted));
        }

        setRecipeData(player, recipe, unlock ? getDefaultRequiredMastery(recipe) : 0);
    }

    public static void unlockRecipes(Player player, List<String> recipes) {
        for (String recipe : recipes) {
            updateRecipeStatus(player, recipe, true);
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
        unlockRecipes(player, Collections.singletonList(randomRecipe));
    }

    public static void unlockAllRecipes(Player player) {
        List<String> unlockedRecipes = getUnlockedRecipes(player);
        List<String> lockedRecipes = getLockedRecipes(unlockedRecipes);
        unlockRecipes(player, lockedRecipes);
    }

    public static void unlockStarterRecipes(Player player) {
        List<String> unlockedRecipes = getUnlockedRecipes(player);
        List<String> recipesToUnlock = new ArrayList<>();

        for (String recipe : ConfigManager.starterRecipes) {
            if (!unlockedRecipes.contains(recipe)) {
                recipesToUnlock.add(recipe);
            }
        }

        unlockRecipes(player, recipesToUnlock);
    }

    public static void setRecipeStatus(Player player, String recipe, boolean unlock) {
        List<String> unlockedRecipes = getUnlockedRecipes(player);
        if (unlock) {
            if (unlockedRecipes.contains(recipe)) {
                return; // Recipe already unlocked
            }
            updateRecipeStatus(player, recipe, true);
            String recipeFormatted = RECIPES.get(recipe).getNick();
            ItemsAdder.playTotemAnimation(player, recipe + "_particle");
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.recipeUnlocked.replace("{recipe}", recipeFormatted));
        } else {
            if (!unlockedRecipes.contains(recipe)) {
                return; // Recipe already locked
            }
            updateRecipeStatus(player, recipe, false);
            String recipeFormatted = RECIPES.get(recipe).getNick();
            ItemsAdder.playTotemAnimation(player, recipe + "_particle");
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.recipeLocked.replace("{recipe}", recipeFormatted));
        }
    }

    public static List<String> getUnlockedRecipes(Player player) {
        YamlConfiguration config = getConfig("playerdata.yml");
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

    public static void setRecipeData(Player player, String recipe, int count) {
        YamlConfiguration config = getConfig("playerdata.yml");
        File file = new File(CustomCooking.plugin.getDataFolder(), "playerdata.yml");

        String playerName = player.getName();
        String playerRecipePath = "players." + playerName + "." + recipe;

        config.set(playerRecipePath, count);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int requiredMastery = getDefaultRequiredMastery(recipe);
        if (count >= requiredMastery) {
            config.set(playerRecipePath, requiredMastery);
            ItemsAdder.playTotemAnimation(player, recipe + "_particle");
            AdventureUtil.consoleMessage(MessageManager.prefix + "Player <green>" + playerName + "</green> has achieved mastery for " + recipe);
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.masteryMessage.replace("{recipe}", RECIPES.get(recipe).getNick()));
        }
    }

    public static boolean playerDataExists(Player player){
        YamlConfiguration config = getConfig("playerdata.yml");
        String playerName = player.getName();
        String playerDataPath = "players." + playerName;

        return config.contains(playerDataPath);
    }
}
