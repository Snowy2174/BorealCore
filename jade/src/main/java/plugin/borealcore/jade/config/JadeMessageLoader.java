package plugin.borealcore.jade.config;

import org.bukkit.configuration.ConfigurationSection;
import plugin.borealcore.manager.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads jade-specific messages from the messages file.
 * Called by JadeModule during module initialization.
 * <p>
 * Uses ModuleContext pattern: All message defaults are applied automatically,
 * and messages are read from the messages_<lang>.yml file.
 */
public class JadeMessageLoader {

    public static void load() {
        Map<String, String> messageDefaults = new HashMap<>();

        messageDefaults.put("jade-first-time", "This is the first time you've gotten Jade from {source} today, you have {limit} remaining.");
        messageDefaults.put("jade-received", "You have received {amount} Jade.");
        messageDefaults.put("jade-limit-reached", "You've reached your limit for Jade from {source} today, try again later.");
        messageDefaults.put("jade-broadcast", "Whilst {source}, {player} has found {amount}₪.");
        messageDefaults.put("jade-cooldown", "You need to wait {time} seconds before earning jade from this source.");
        messageDefaults.put("jade-source-not-found", "That source does not exist.");
        messageDefaults.put("jade-limit-header", "<gradient:#00AA00:#88DAA1>\n}======------ Jade Source Limits ------======={</gradient>\n");
        messageDefaults.put("jade-limit-source", "<gradient:#00AA00:#88DAA1>  --> {source}: {total} / {limit} </gradient>");
        messageDefaults.put("jade-limit-footer", "<gradient:#00AA00:#88DAA1>\n[______________________]</gradient>\n");
        messageDefaults.put("jade-get-started", "<gradient:#00AA00:#88DAA1>Get started with Jade by using /cooking jade</gradient>");
        messageDefaults.put("jade-source-reminder", "<gradient:#00AA00:#88DAA1>Click here to view your limits <click:run_command:jade limits>[ ! ]</gradient>");
        messageDefaults.put("jade-source-reminder-2", "<gradient:#00AA00:#88DAA1>Cwor check out the wiki for more info</gradient>");
        messageDefaults.put("leaderboard-header", "<gradient:#00AA00:#88DAA1>\n}======------ {type} Leaderboard || Page {page} / {totalPages} ------======={</gradient>\n");
        messageDefaults.put("leaderboard-entry", "<gradient:#00AA00:#88DAA1>  --> {position}: {player} - {score} </gradient>");
        messageDefaults.put("alt-leaderboard-entry", "&e{position}. &b{player} &7- &e{score}");
        messageDefaults.put("leaderboard-footer", "<gradient:#00AA00:#88DAA1>\n[______________________]</gradient>\n");

        ConfigurationSection messages = ConfigManager.setupModuleMessages(messageDefaults);

        JadeMessage.jadeFirstTime = messages.getString("messages.jade-first-time", "This is the first time you've gotten Jade from {source} today, you have {limit} remaining.");
        JadeMessage.jadeReceived = messages.getString("messages.jade-received", "You have received {amount} Jade.");
        JadeMessage.jadeLimitReached = messages.getString("messages.jade-limit-reached", "You've reached your limit for Jade from {source} today, try again later.");
        JadeMessage.jadeBroadcast = messages.getString("messages.jade-broadcast", "Whilst {source}, {player} has found {amount}₪.");
        JadeMessage.jadeCooldown = messages.getString("messages.jade-cooldown", "You need to wait {time} seconds before earning jade from this source.");
        JadeMessage.jadeSourceNotFound = messages.getString("messages.jade-source-not-found", "That source does not exist.");
        JadeMessage.jadeLimitHeader = messages.getString("messages.jade-limit-header", "<gradient:#00AA00:#88DAA1>\n}======------ Jade Source Limits ------======={</gradient>\n");
        JadeMessage.jadeLimitSource = messages.getString("messages.jade-limit-source", "<gradient:#00AA00:#88DAA1>  --> {source}: {total} / {limit} </gradient>");
        JadeMessage.jadeLimitFooter = messages.getString("messages.jade-limit-footer", "<gradient:#00AA00:#88DAA1>\n[______________________]</gradient>\n");
        JadeMessage.jadeGetStarted = messages.getString("messages.jade-get-started", "<gradient:#00AA00:#88DAA1>Get started with Jade by using /cooking jade</gradient>");
        JadeMessage.jadeSourceReminder = messages.getString("messages.jade-source-reminder", "<gradient:#00AA00:#88DAA1>Click here to view your limits <click:run_command:jade limits>[ ! ]</gradient>");
        JadeMessage.jadeSourceReminder2 = messages.getString("messages.jade-source-reminder-2", "<gradient:#00AA00:#88DAA1>Cwor check out the wiki for more info</gradient>");

        JadeMessage.leaderboardHeader = messages.getString("messages.leaderboard-header", "<gradient:#00AA00:#88DAA1>\n}======------ {type} Leaderboard || Page {page} / {totalPages} ------======={</gradient>\n");
        JadeMessage.leaderboardEntry = messages.getString("messages.leaderboard-entry", "<gradient:#00AA00:#88DAA1>  --> {position}: {player} - {score} </gradient>");
        JadeMessage.altLeaderboardEntry = messages.getString("messages.alt-leaderboard-entry", "&e{position}. &b{player} &7- &e{score}");
        JadeMessage.leaderboardFooter = messages.getString("messages.leaderboard-footer", "<gradient:#00AA00:#88DAA1>\n[______________________]</gradient>\n");
    }
}


