package plugin.customcooking.functions.jade;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.api.event.CookResultEvent;
import plugin.customcooking.api.event.JadeEvent;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;
import plugin.customcooking.utility.ConfigUtil;
import plugin.customcooking.utility.GUIUtil;
import plugin.customcooking.utility.InventoryUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class JadeManager extends Function {

    protected static Database database;
    public static HashMap<String, Integer> jadeLimit = new HashMap<>();

    @Override
    public void load() {
        AdventureUtil.consoleMessage("[CustomCooking] Initialised Jade limit system");
        jadeLimit = loadJadeLimits();
        AdventureUtil.consoleMessage("[JadeManager] Loaded Jade limits: " + jadeLimit.toString());
        database = CustomCooking.getDatabase();
        database.verifyAndFixTotals();

        // Check for sources in the database not in jadeLimit
        List<String> sources = database.getAllSources();
        for (String source : sources) {
            if (!jadeLimit.containsKey(source)) {
                AdventureUtil.consoleMessage("[JadeManager] Warning: Source '" + source + "' is not defined in jade limits.");
            }
        }
    }

    @Override
    public void unload() {
        jadeLimit.clear();
    }

    private HashMap<String, Integer> loadJadeLimits() {
        YamlConfiguration config = ConfigUtil.getConfig("config.yml");
        HashMap<String, Integer> jadeLimit = new HashMap<>();
        for (String key : config.getConfigurationSection("jade.limits").getKeys(false)) {
            int limit = config.getInt("jade.limits." + key);
            jadeLimit.put(key, limit);
        }
        return jadeLimit;
    }

    public HashMap<String, Integer> getJadeLeaderboard() {
        return database.getJadeLeaderboard();
    }

    public static void giveJadeCommand(Player player, String source, Integer amount) {
        if (database.getRecentPositiveTransactionTimestamps(player, source).size() <= getLimitForSource(source)) {
            give(player, amount, source);
        } else {
            AdventureUtil.sendMessage(player, MessageManager.jadeLimitReached
                    .replace("{source}", GUIUtil.formatString(source)));
        }
    }

    public static void give(Player player, double amount, String source) {
        boolean first = source.isBlank() || database.getRecentPositiveTransactionTimestamps(player, source).isEmpty();

        if (first && !source.isEmpty()) {
            AdventureUtil.sendMessage(player, MessageManager.jadeFirstTime
                    .replace("{source}", GUIUtil.formatString(source))
                    .replace("{limit}", String.valueOf(getLimitForSource(source))));
        } else {
            AdventureUtil.sendMessage(player, MessageManager.jadeReceived
                    .replace("{amount}", String.valueOf(amount)));
        }

        String command = "av User " + player.getName() + " AddPoints " + (int) amount;
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);

        database.addTransaction(player, amount, source, LocalDateTime.now());

        JadeEvent jadeEvent = new JadeEvent(player, amount, source);
        Bukkit.getPluginManager().callEvent(jadeEvent);

        String bcast = MessageManager.jadeBroadcast
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
        if (source.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return jadeLimit.getOrDefault(source, 100);
    }

    public static String checkJadeLimit(Player player, String source) {
        return database.getRecentPositiveTransactionTimestamps(player, source).size() + "/" + getLimitForSource(source);
    }

    public static int getTotalJadeForPlayer(Player player) {
        return database.getJadeForPlayer(player);
    }

    // @TODO Implement migrate Legacy jade data
}