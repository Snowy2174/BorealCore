package plugin.customcooking.configs;

import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.minigame.Function;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;

import java.io.File;
import java.io.IOException;

import static plugin.customcooking.configs.RecipeManager.RECIPES;
import static plugin.customcooking.configs.RecipeManager.masteryreqs;

public class MasteryManager extends Function {

    @Override
    public void load() {
        AdventureUtil.consoleMessage("[CustomCooking] Loaded mastery values");
    }

    public static void handleMastery(Player player, String recipe) {
        YamlConfiguration config = ConfigUtil.getConfig("playerdata.yml");
        File file = new File(CustomCooking.plugin.getDataFolder(), "playerdata.yml");

        String playerName = player.getName();
        String playerRecipePath = "players." + playerName + "." + recipe;

        if (!config.contains(playerRecipePath)) {
            config.set(playerRecipePath, 0);
        }

        int count = config.getInt(playerRecipePath);
        count++;
        config.set(playerRecipePath, count);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int requiredMastery = getRequiredMastery(recipe);
        if (count >= requiredMastery) {
            String recipeFormatted = RECIPES.get(recipe).getNick();

            ItemsAdder.playTotemAnimation(player, recipe + "_particle");
            AdventureUtil.consoleMessage("[CustomCooking] Player <green>" + playerName + "</green> has achieved mastery for " + recipe);
            AdventureUtil.playerMessage(player, "<gray>[<green><bold>!</bold><gray>] <green>You have achieved mastery for the dish: " + recipeFormatted);

            // Give the player reward
            giveReward(player, recipeFormatted);
        }
    }

    private static void giveReward(Player player, String recipeFormatted) {
        // Give the player a reward (e.g., points)
        String command = "av user " + player.getName() + " addpoints 5";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        AdventureUtil.consoleMessage("[CustomCooking] Player <green>" + player.getName() + "</green> has been given 5 ₪ for gaining " + recipeFormatted + " mastery");
        AdventureUtil.playerMessage(player, "<gray>[<green><bold>!</bold><gray>] <green>You have been given 5 ₪ for gaining " + recipeFormatted + " mastery");
    }

    public static int getMasteryCount(Player player, String recipe) {
        YamlConfiguration config = ConfigUtil.getConfig("playerdata.yml");
        int count = config.getInt("players." + player.getName() + "." + recipe, 0);
        return count;
    }

    public static int getRequiredMastery(String recipe) {
        Integer mastery = masteryreqs.get(recipe);
        if (mastery == null) {
            System.out.println("No Mastery Value found for " + recipe);
            return 10;
        }
        return mastery;
    }

    public static boolean hasMastery(Player player, String recipe) {
        int count = getMasteryCount(player, recipe);
        int requiredMastery = getRequiredMastery(recipe);
        return count >= requiredMastery;
    }
}
