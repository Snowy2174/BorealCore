package plugin.customcooking.util;

import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.configs.MessageManager;

import java.io.File;
import java.io.IOException;

import static plugin.customcooking.manager.RecipeManager.RECIPES;
import static plugin.customcooking.manager.configs.MasteryManager.getRequiredMastery;

public class RecipeDataUtil {


    public static void setRecipeData(Player player, String recipe, int count) {
        YamlConfiguration config = ConfigUtil.getConfig("playerdata.yml");
        File file = new File(CustomCooking.plugin.getDataFolder(), "playerdata.yml");

        String playerName = player.getName();
        String playerRecipePath = "players." + playerName + "." + recipe;

        config.set(playerRecipePath, count);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int requiredMastery = getRequiredMastery(recipe);
        if (count >= requiredMastery) {
            config.set(playerRecipePath, requiredMastery);
            ItemsAdder.playTotemAnimation(player, recipe + "_particle");
            AdventureUtil.consoleMessage(MessageManager.prefix + "Player <green>" + playerName + "</green> has achieved mastery for " + recipe);
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.masteryMessage.replace("{recipe}", RECIPES.get(recipe).getNick()));
        }
    }

    public static void removeRecipeMasteryData(Player player, String recipe) {
        YamlConfiguration config = ConfigUtil.getConfig("playerdata.yml");
        String playerName = player.getName();
        String playerRecipePath = "players." + playerName + "." + recipe;

        if (config.contains(playerRecipePath)) {
            config.set(playerRecipePath, null);
            File file = new File(CustomCooking.plugin.getDataFolder(), "playerdata.yml");
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean playerDataExists(Player player){
        YamlConfiguration config = ConfigUtil.getConfig("playerdata.yml");
        String playerName = player.getName();
        String playerDataPath = "players." + playerName;

        return config.contains(playerDataPath);
    }
}
