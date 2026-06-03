package plugin.borealcore.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import plugin.borealcore.BorealCore;

import java.io.File;
import java.io.IOException;

public class MessageManager {

    public static String prefix;
    public static String infoNegative;
    public static String infoPositive;
    public static String reload;
    public static String noPerms;
    public static String nonArgs;
    public static String unavailableArgs;
    public static String itemNotExist;
    public static String playerNotExist;
    public static String noConsole;
    public static String wrongAmount;
    public static String lackArgs;
    public static String notEnoughPlayers;
    public static String notOnline;
    public static String pluginError;
    public static String TOTAL_SCORE;
    public static String CATCH_AMOUNT;
    public static String actionBarHealth;

    public static void load() {
        YamlConfiguration config = ConfigManager.getConfig("messages_" + ConfigManager.lang + ".yml");
        prefix = getOrSet(config, "prefix", "<gradient:#FB5A00:#FDF300>[BorealCore] </gradient>");
        infoNegative = getOrSet(config, "prefix-negative", "<gray>[<red><bold>!</bold><gray>]<red> ");
        infoPositive = getOrSet(config, "prefix-positive", "<gray>[<green><bold>!</bold><gray>]<green> ");

        reload = getOrSet(config, "reload", "<white>Reloaded. Took <green>{time}ms.");
        noPerms = getOrSet(config, "no-perms", "<red>You don't have permission to use this command.");
        nonArgs = getOrSet(config, "none-args", "Arguments cannot be none.");
        unavailableArgs = getOrSet(config, "invalid-args", "Invalid arguments.");
        itemNotExist = getOrSet(config, "item-not-exist", "That item does not exist.");
        playerNotExist = getOrSet(config, "player-not-exist", "That player does not exist.");
        noConsole = getOrSet(config, "no-console", "This command cannot be executed from the console.");
        wrongAmount = getOrSet(config, "wrong-amount", "You can''t set an negative amount of items.");
        lackArgs = getOrSet(config, "lack-args", "Insufficient arguments.");

        notEnoughPlayers = getOrSet(config, "players-not-enough", "The number of players who can cook is not enough for the cooking competition to be started as scheduled.");
        notOnline = getOrSet(config, "not-online", "That player is not online.");
        pluginError = getOrSet(config, "plugin-error", "<red>Please contact @Snow'eh on discord with a full report of this error");
        TOTAL_SCORE = getOrSet(config, "total_score", "Total score");
        CATCH_AMOUNT = getOrSet(config, "catch_amount", "Catch amount");

        actionBarHealth = getOrSet(config, "action-bar-health", "<gray>%s: <red>%d / %.0f <grey>| %s: <red>%d / %.0f");

        try {
            config.save(new File(BorealCore.getInstance().getDataFolder(), "messages_" + ConfigManager.lang + ".yml"));
        } catch (IOException ignore) {
        }
    }

    private static String getOrSet(ConfigurationSection section, String path, String defaultValue) {
        path = "messages." + path;
        if (!section.contains(path)) {
            section.set(path, defaultValue);
        }
        return section.getString(path);
    }
}
