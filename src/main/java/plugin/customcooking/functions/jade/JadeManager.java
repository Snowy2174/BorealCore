package plugin.customcooking.functions.jade;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import plugin.customcooking.api.event.JadeEvent;
import plugin.customcooking.database.Database;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;
import plugin.customcooking.utility.ConfigUtil;
import plugin.customcooking.utility.GUIUtil;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.bukkit.Bukkit.getServer;

public class JadeManager extends Function {

    protected static Database database;
    public static HashMap<String, JadeSource> jadeSources = new HashMap<>();

    public JadeManager(Database database) {
        this.database = database;
    }

    @Override
    public void load() {
        loadJadeLimits();
        database.verifyAndFixTotals();
        checkUndefinedSources();
    }

    @Override
    public void unload() {
        jadeSources.clear();
    }

    private void checkUndefinedSources() {
        for (String source : database.getAllSources()) {
            if (!jadeSources.containsKey(source)) {
                System.out.println("[JadeManager] Warning: Undefined source '" + source + "'.");
            }
        }
    }

private void loadJadeLimits() {
                YamlConfiguration config = ConfigUtil.getConfig("config.yml");
                for (String key : config.getConfigurationSection("jade.sources").getKeys(false)) {
                    int limit = config.getInt("jade.sources." + key + ".limit");
                    long cooldown = config.getLong("jade.sources." + key + ".cooldown", 0);
                    jadeSources.put(key, new JadeSource(key, cooldown, limit));
                }
                AdventureUtil.consoleMessage("[CustomCooking] Initialised Jade limit system");
                AdventureUtil.consoleMessage("[JadeManager] Loaded Jade limits: " + jadeSources.toString());
            }

    public HashMap<String, Integer> getJadeLeaderboard() {
        return database.getJadeLeaderboard();
    }

    public static void giveJadeCommand(Player player, String source, Integer amount) {
        // Reconsile Jade data
        if (reconsileJadeData(player)) {
            AdventureUtil.consoleMessage( MessageManager.infoPositive + "Jade data has been reconsiled for " + player.getName());
            return;
        }

        // Check if player is on cooldown
        if (database.isOnCooldown(player, source)) {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + MessageManager.jadeCooldown
                    .replace("{time}", String.valueOf(database.getCooldownTimeLeft(player, source))));
            return;
        }
        // Check if player has reached Limit
        if (database.getRecentPositiveTransactionTimestamps(player, source).size() <= getLimitForSource(source)) {
            give(player, amount, source);
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + MessageManager.jadeLimitReached
                    .replace("{source}", GUIUtil.formatString(source)));
        }
    }

    public static void give(Player player, double amount, String source) {
        boolean first = source.isBlank() || database.getRecentPositiveTransactionTimestamps(player, source).isEmpty();

        if (first && !source.isEmpty()) {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + MessageManager.jadeFirstTime
                    .replace("{source}", GUIUtil.formatString(source))
                    .replace("{limit}", String.valueOf(getLimitForSource(source))));
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + MessageManager.jadeReceived
                    .replace("{amount}", String.valueOf(amount)));
        }

        // Temp until migration is done
        String command = "av User " + player.getName() + " AddPoints " + (int) amount;
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);

        database.addTransaction(player, amount, source, LocalDateTime.now());

        JadeEvent jadeEvent = new JadeEvent(player, amount, source);
        Bukkit.getPluginManager().callEvent(jadeEvent);

        String bcast = MessageManager.infoPositive + MessageManager.jadeBroadcast
                .replace("{source}", source.isEmpty() ? "playing" : GUIUtil.formatString(source))
                .replace("{player}", player.getName())
                .replace("{amount}", String.valueOf((int) amount));
        getServer().broadcast(AdventureUtil.getComponentFromMiniMessage(bcast));
    }

    public static void remove(Player player, double amount, String source) {
        String command = "av User " + player.getName() + " RemovePoints " + (int) amount;
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);
        database.addTransaction(player, -amount, source, LocalDateTime.now());
    }

    public static int getLimitForSource(String source) {
        if (source.isEmpty()) {return Integer.MAX_VALUE;}
        return jadeSources.get(source).getLimit();
    }

    public static String checkJadeLimit(Player player, String source) {
        return database.getRecentPositiveTransactionTimestamps(player, source).size() + "/" + getLimitForSource(source);
    }

    public static int getTotalJadeForPlayer(Player player) {
        return database.getJadeForPlayer(player);
    }
    // @TODO Implement migrate Legacy jade data

    public static boolean reconsileJadeData(Player player) {
        int avPoints = Integer.parseInt(PlaceholderAPI.setPlaceholders(player, "%VotingPlugin_Points%"));
        if (avPoints == database.getJadeForPlayer(player)) {
            System.out.println("Jade data is already reconciled for " + player.getName() + " or " + avPoints + " jade");
            return false;
        }
        System.out.println("Old jade " + avPoints + " new jade " + database.getJadeForPlayer(player));

        int jadeDiff = avPoints - database.getJadeForPlayer(player);
        if (jadeDiff > 0) {
            System.out.println("Positive jade diff: " + jadeDiff);
        } else {
            System.out.println("Negative jade diff: " + jadeDiff);
        }
        return true;
    }
}