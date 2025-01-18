package plugin.customcooking.manager;

import dev.lone.itemsadder.api.ItemsAdder;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.util.RecipeDataUtil;

import java.io.File;
import java.io.IOException;

import static plugin.customcooking.manager.configs.RecipeManager.RECIPES;
import static plugin.customcooking.util.ConfigUtil.getConfig;

public class DataManager extends Function {


    @Override
    public void load() {
        AdventureUtil.consoleMessage(MessageManager.prefix + "Loaded mastery values");
    }

    @Override
    public void unload() {
        savePlayerStats(getConfig("playerstats.yml"));
        AdventureUtil.consoleMessage(MessageManager.prefix + "Unloaded mastery values");
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

        int requiredMastery = RecipeDataUtil.getDefaultRequiredMastery(recipe);
        if (count >= requiredMastery) {
            String recipeFormatted = RECIPES.get(recipe).getNick();

            ItemsAdder.playTotemAnimation(player, recipe + "_particle");
            AdventureUtil.consoleMessage(MessageManager.prefix + "Player <green>" + playerName + "</green> has achieved mastery for " + recipe);
            AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.masteryMessage.replace("{recipe}", recipeFormatted ));

            // Give the player reward
            giveReward(player, recipeFormatted);
        }
    }

    private static void giveReward(Player player, String recipeFormatted) {
        // Give the player a reward (e.g., points)
        JadeManager.give(player, ConfigManager.masteryJadeReward, "mastery");

        AdventureUtil.consoleMessage(MessageManager.prefix + "Player <green>" + player.getName() + "</green> has been given" + ConfigManager.masteryJadeReward + " ₪ for gaining " + recipeFormatted + " mastery");
        AdventureUtil.playerMessage(player, MessageManager.infoPositive + MessageManager.masteryReward.replace("{recipe}", recipeFormatted));
    }

    public static void incrementRecipeCount(Player player) {
        FileConfiguration config = getConfig("playerstats.yml");
        String playerName = player.getName();
        int currentCount = config.getInt("players." + playerName, 0);
        int newCount = currentCount + 1;
        config.set("players." + playerName, newCount);
        savePlayerStats(config);
    }

    public static int getRecipeCount(String playerName) {
        FileConfiguration config = getConfig("playerstats.yml");
        return config.getInt("players." + playerName, 0);
    }

    private static void savePlayerStats(FileConfiguration config) {
        File file = new File(CustomCooking.plugin.getDataFolder(), "playerstats.yml");
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Permissions Migrater
    public static int migratePermissions() {
        String permissionPrefix = "customcooking.";
        int migratedCount = 0;

        LuckPerms luckPerms = LuckPermsProvider.get();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                for (PermissionAttachmentInfo attachmentInfo : player.getEffectivePermissions()) {
                    String permission = attachmentInfo.getPermission();

                    if (permission.startsWith(permissionPrefix + "recipe.")) {
                        String recipeName = permission.substring(permissionPrefix.length() + 7);
                        RecipeDataUtil.setRecipeStatus(player, recipeName, true);
                        user.data().remove(Node.builder(permission).build());
                        migratedCount++;
                    }
                    if (permission.startsWith(permissionPrefix + "mastery.")) {
                        String recipeName = permission.substring(permissionPrefix.length() + 8);
                        RecipeDataUtil.setRecipeData(player, recipeName, 1000);
                        user.data().remove(Node.builder(permission).build());
                        migratedCount++;
                    }
                }
                luckPerms.getUserManager().saveUser(user);
            }
        }
        return migratedCount;
    }

}
