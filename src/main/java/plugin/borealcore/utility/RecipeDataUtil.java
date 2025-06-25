package plugin.borealcore.utility;

import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.configs.ConfigManager;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.functions.cooking.configs.RecipeManager;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static plugin.borealcore.utility.ConfigUtil.getConfig;

public class RecipeDataUtil {

    public static int getMasteryCount(Player player, String recipe) {
        YamlConfiguration config = getConfig("data/playerdata.yml");
        int count = config.getInt("players." + player.getName() + "." + recipe, 0);
        return count;
    }

    public static int getDefaultRequiredMastery(String recipe) {
        Integer mastery = RecipeManager.COOKING_RECIPES.get(recipe).getMasteryreq();
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

    public static void unlockRecipes(Player player, List<String> recipes) {
        for (String recipe : recipes) {
            setRecipeStatus(player, recipe, true);
        }
    }

    public static void checkAndAddRandomRecipe(Player player) {
        List<String> unlockedRecipes = getUnlockedRecipes(player);
        List<String> lockedRecipes = getLockedRecipes(unlockedRecipes);

        if (lockedRecipes.isEmpty()) {
            // Player has unlocked all recipes
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + "You have already unlocked all recipes!");
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

    public static List<String> getMasteredRecipes(Player player, List<String> unlockedRecipes) {
        List<String> masteredRecipes = new ArrayList<>();
        for (String recipe : unlockedRecipes) {
            int count = getMasteryCount(player, recipe);
            int requiredMastery = getDefaultRequiredMastery(recipe);
            if (count >= requiredMastery) {
                masteredRecipes.add(recipe);
            }
        }
        return masteredRecipes;
    }

    public static List<String> getUnlockedRecipes(Player player) {
        YamlConfiguration config = getConfig("data/playerdata.yml");
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

    public static List<String> getLockedRecipes(List<String> unlockedRecipes) {
        return RecipeManager.COOKING_RECIPES.keySet().stream()
                .filter(recipe -> !unlockedRecipes.contains(recipe))
                .collect(Collectors.toList());
    }

    private static String getRandomRecipe(List<String> recipes) {
        Random random = new Random();
        int index = random.nextInt(recipes.size());
        return recipes.get(index);
    }

    public static void setRecipeStatus(Player player, String recipe, boolean unlock) {
        List<String> unlockedRecipes = getUnlockedRecipes(player);
        if (unlock) {
            if (unlockedRecipes.contains(recipe)) {
                return;
            }
            setRecipeData(player, recipe, 0);
            String recipeFormatted = RecipeManager.COOKING_RECIPES.get(recipe).getNick();
            ItemsAdder.playTotemAnimation(player, recipe + ConfigManager.particleItemSuffix);
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.recipeUnlocked.replace("{recipe}", recipeFormatted));
        } else {
            if (!unlockedRecipes.contains(recipe)) {
                return;
            }
            setRecipeData(player, recipe, null);
            String recipeFormatted = RecipeManager.COOKING_RECIPES.get(recipe).getNick();
            ItemsAdder.playTotemAnimation(player, recipe + ConfigManager.particleItemSuffix);
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + MessageManager.recipeLocked.replace("{recipe}", recipeFormatted));
        }
    }

    public static void setRecipeData(Player player, String recipe, @Nullable Integer count) {
        YamlConfiguration config = getConfig("data/playerdata.yml");
        File file = new File(BorealCore.plugin.getDataFolder(), "data/playerdata.yml");

        String playerName = player.getName();
        String playerRecipePath = "players." + playerName + "." + recipe;

        config.set(playerRecipePath, count);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int requiredMastery = getDefaultRequiredMastery(recipe);
        if (count != null && count >= requiredMastery) {
            config.set(playerRecipePath, requiredMastery);
            ItemsAdder.playTotemAnimation(player, recipe + ConfigManager.particleItemSuffix);
            AdventureUtil.consoleMessage(MessageManager.prefix + "Player <green>" + playerName + "</green> has achieved mastery for " + recipe);
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.masteryMessage.replace("{recipe}", RecipeManager.COOKING_RECIPES.get(recipe).getNick()));
        }
    }

    public static boolean playerDataExists(Player player) {
        YamlConfiguration config = getConfig("data/playerdata.yml");
        String playerName = player.getName();
        String playerDataPath = "players." + playerName;

        return config.contains(playerDataPath);
    }
}
