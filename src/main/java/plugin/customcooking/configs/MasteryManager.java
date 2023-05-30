package plugin.customcooking.configs;

import dev.lone.itemsadder.api.ItemsAdder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.minigame.Function;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.util.PermUtil;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static plugin.customcooking.configs.RecipeManager.masteryreqs;

public class MasteryManager extends Function {

    @Override
    public void load() {
        AdventureUtil.consoleMessage("[CustomCooking] Loaded mastery values");
    }

    public static void handleMastery(Player player, String recipe) {

        YamlConfiguration config = ConfigUtil.getConfig("playerdata.yml");
        File file = new File(CustomCooking.plugin.getDataFolder(), "playerdata.yml");

        // retrieve the player's name or unique identifier and the task identifier
        String playerName = player.getName();
        // if the player value doesn't exist, create it and set the count to 0
        if (!config.contains("players." + playerName)) {
            config.createSection("players." + playerName);
        }
        // if the task identifier value doesn't exist, create it and set the count to 0
        if (!config.contains("players." + playerName + "." + recipe)) {
            config.set("players." + playerName + "." + recipe, 0);
        }
        // increment the count and update the YAML file
        int count = config.getInt("players." + playerName + "." + recipe, 0);
        count++;
        config.set("players." + playerName + "." + recipe, count);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace(); // Print the error message to help debug the issue
            return; // Exit the method early to prevent further errors
        }

        AdventureUtil.consoleMessage("[CustomCooking] Player " + playerName + "has achieved <green>" + count + "/" + masteryreqs.get(recipe) + "</green> for the dish <green>" + recipe);
        AdventureUtil.playerMessage(player, "<gray>[<green><bold>!</bold><gray>] <green>You have cooked this dish perfectly <gold>" + count + "/" + masteryreqs.get(recipe) + "<green> times to achieve mastery!");

        if (count >= masteryreqs.get(recipe)) {
            // give the player the permission
            ItemsAdder.playTotemAnimation(player, recipe+"_particle");
            PermUtil.addPermission(player.getUniqueId(), "customcooking.mastery." + recipe);
            AdventureUtil.consoleMessage("[CustomCooking] Player <green>" + playerName + "</green> has been given <green>" + "customcooking.mastery." + recipe + "");
            AdventureUtil.playerMessage(player, "<gray>[<green><bold>!</bold><gray>] <green>You have been given 5 ₪ for gaining " + recipe + " mastery");

            String command = "av user " + playerName + " addpoints 5";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            // clear the task data in the config
            config.set("players." + playerName + "." + recipe, null);
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace(); // Print the error message to help debug the issue
                return; // Exit the method early to prevent further errors
            }
        }
    }

    public static void setMasteryCount(Player player, String recipe, int count) {
        YamlConfiguration config = ConfigUtil.getConfig("playerdata.yml");
        File file = new File(CustomCooking.plugin.getDataFolder(), "playerdata.yml");

        String playerName = player.getName();
        String playerRecipePath = "players." + playerName + "." + recipe;

        if (config.contains(playerRecipePath)) {
            config.set(playerRecipePath, count);
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int requiredMastery = getRequiredMastery(recipe);
            if (count >= requiredMastery) {
                PermUtil.addPermission(player.getUniqueId(), "customcooking.mastery." + recipe);

                ItemsAdder.playTotemAnimation(player, recipe+"_particle");
                AdventureUtil.consoleMessage("[CustomCooking] Player <green>" + playerName + "</green> has been given <green>customcooking.mastery." + recipe + "");
                AdventureUtil.playerMessage(player, "<gray>[<green><bold>!</bold><gray>] <green>You have achieved mastery for the dish: " + recipe);
            }
        } else {
            AdventureUtil.consoleMessage("[CustomCooking] Player " + playerName + " does not have a mastery count for recipe " + recipe);
        }
    }

    public static int getMasteryCount(Player player, String recipe) {
        YamlConfiguration config = ConfigUtil.getConfig("playerdata.yml");
        int count = config.getInt("players." + player.getName() + "." + recipe, 0);
        return count;
    }

    public static int getRequiredMastery(String recipe) {
        Integer mastery = masteryreqs.get(recipe);
        if (mastery == null) {
            // return a default value or throw an exception as needed
            System.out.println("No Mastery Value found for " + recipe);
            return 10;
        }
        return mastery.intValue();
    }
}
