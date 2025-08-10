package plugin.borealcore.manager.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import plugin.borealcore.BorealCore;
import plugin.borealcore.utility.ConfigUtil;

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
    public static String noRank;
    public static String competitionOn;
    public static String forceSuccess;
    public static String forceFailure;
    public static String forceEnd;
    public static String forceCancel;
    public static String noPlayer;
    public static String noScore;
    public static String masteryMessage;
    public static String masteryReward;
    public static String recipeLocked;
    public static String recipeUnlocked;
    public static String recipeUnknown;
    public static String recipeNoPot;
    public static String alreadyCooking;
    public static String noIngredients;
    public static String tooSlow;
    public static String cookingAutocooked;
    public static String cookingPerfect;
    public static String potLight;
    public static String potCold;
    public static String potCooldown;
    public static String notOnline;
    public static String pluginError;
    public static String TOTAL_SCORE;
    public static String CATCH_AMOUNT;
    public static String jadeFirstTime;
    public static String jadeReceived;
    public static String jadeLimitReached;
    public static String jadeBroadcast;
    public static String jadeCooldown;
    public static String jadeSourceNotFound;
    public static String jadeLimitHeader;
    public static String jadeLimitSource;
    public static String jadeLimitFooter;
    public static String jadeGetStarted;
    public static String jadeSourceReminder;
    public static String jadeSourceReminder2;
    public static String leaderboardHeader;
    public static String leaderboardEntry;
    public static String altLeaderboardEntry;
    public static String leaderboardFooter;
    public static String actionBarHealth;

    public static void load() {
        YamlConfiguration config = ConfigUtil.getConfig("messages_" + ConfigManager.lang + ".yml");
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

        masteryMessage = getOrSet(config, "mastery-message", "<green>You have achieved mastery for the dish: {recipe}");
        masteryReward = getOrSet(config, "mastery-reward", "<green>You have been given 5 ₪ for gaining {recipe} mastery");

        recipeLocked = getOrSet(config, "recipe-locked", "<red>You have lost the recipe: {recipe}");
        recipeUnlocked = getOrSet(config, "recipe-unlocked", "<green>You have unlocked the recipe: {recipe}");
        recipeUnknown = getOrSet(config, "recipe-unknown", "<red>You haven't unlocked this recipe yet..");
        recipeNoPot = getOrSet(config, "recipe-no-pot", "<red>You need to use a cooking pot to cook this recipe!");

        alreadyCooking = getOrSet(config, "already-cooking", "<red>You're already cooking something!");
        noIngredients = getOrSet(config, "no-ingredients", "<red>You do not have the required ingredients to cook this item.");
        tooSlow = getOrSet(config, "too-slow", "You've failed to produce the dish in the required time");
        cookingAutocooked = getOrSet(config, "cooking-autocooked", "<green>You have autocooked one {recipe}");
        cookingPerfect = getOrSet(config, "cooking-perfect", "<green>You have cooked the dish {recipe} perfectly!");

        potLight = getOrSet(config, "pot-light-up", "<green>You lit up the cooking pot!");
        potCold = getOrSet(config, "pot-cold", "<red>You can't cook int a cold pot.. try heating it up");
        potCooldown = getOrSet(config, "pot-cooldown", "You need to wait {time} seconds before interacting with this again!");

        notOnline = getOrSet(config, "not-online", "That player is not online.");
        notEnoughPlayers = getOrSet(config, "players-not-enough", "The number of players who can cook is not enough for the cooking competition to be started as scheduled.");
        noRank = getOrSet(config, "no-rank", "No Rank");
        competitionOn = getOrSet(config, "competition-ongoing", "There is currently a cooking tournament in progress! Start cooking to join the contest for a prize!");
        forceSuccess = getOrSet(config, "force-competition-success", "Forced to start a cooking competition.");
        forceFailure = getOrSet(config, "force-competition-failure", "The competition does not exist.");
        forceEnd = getOrSet(config, "force-competition-end", "Forced to end the current competition.");
        forceCancel = getOrSet(config, "force-competition-cancel", "Forced to cancel the competition");
        noPlayer = getOrSet(config, "no-player", "No player");
        noScore = getOrSet(config, "no-score", "No score");

        pluginError = getOrSet(config, "plugin-error", "<red>Please contact @Snow'eh on discord with a full report of this error");
        TOTAL_SCORE = getOrSet(config, "total_score", "Total score");
        CATCH_AMOUNT = getOrSet(config, "catch_amount", "Catch amount");

        jadeFirstTime = getOrSet(config, "jade-first-time", "This is the first time you've gotten Jade from {source} today, you have {limit} remaining.");
        jadeReceived = getOrSet(config, "jade-received", "You have received {amount} Jade.");
        jadeLimitReached = getOrSet(config, "jade-limit-reached", "You've reached your limit for Jade from {source} today, try again later.");
        jadeBroadcast = getOrSet(config, "jade-broadcast", "Whilst {source}, {player} has found {amount}₪.");
        jadeCooldown = getOrSet(config, "jade-cooldown", "You need to wait {time} seconds before earning jade from this source.");
        jadeSourceNotFound = getOrSet(config, "jade-source-not-found", "That source does not exist.");
        jadeLimitHeader = getOrSet(config, "jade-limit-header", "<gradient:#00AA00:#88DAA1>\n}======------ Jade Source Limits ------======={</gradient>\n");
        jadeLimitSource = getOrSet(config, "jade-limit-source", "<gradient:#00AA00:#88DAA1>  --> {source}: {total} / {limit} </gradient>");
        jadeLimitFooter = getOrSet(config, "jade-limit-footer", "<gradient:#00AA00:#88DAA1>\n[______________________]</gradient>\n");
        jadeGetStarted = getOrSet(config, "jade-get-started", "<gradient:#00AA00:#88DAA1>Get started with Jade by using /cooking jade</gradient>");
        jadeSourceReminder = getOrSet(config, "jade-source-reminder", "<gradient:#00AA00:#88DAA1>Click here to view your limits <click:run_command:jade limits>[ ! ]</gradient>");
        jadeSourceReminder2 = getOrSet(config, "jade-source-reminder-2", "<gradient:#00AA00:#88DAA1>Cwor check out the wiki for more info</gradient>");

        leaderboardHeader = getOrSet(config, "leaderboard-header", "<gradient:#00AA00:#88DAA1>\n}======------ {type} Leaderboard || Page {page} / {totalPages} ------======={</gradient>\n");
        leaderboardEntry = getOrSet(config, "leaderboard-entry", "<gradient:#00AA00:#88DAA1>  --> {position}: {player} - {score} </gradient>");
        altLeaderboardEntry = getOrSet(config, "alt-leaderboard-entry", "&e{position}. &b{player} &7- &e{score}");
        leaderboardFooter = getOrSet(config, "leaderboard-footer", "<gradient:#00AA00:#88DAA1>\n[______________________]</gradient>\n");

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
