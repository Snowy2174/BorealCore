package plugin.customcooking.functions.jade;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.user.VotingPluginUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.api.event.JadeEvent;
import plugin.customcooking.database.Database;
import plugin.customcooking.functions.brewery.BreweryListener;
import plugin.customcooking.listener.VoteListener;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;
import plugin.customcooking.utility.ConfigUtil;
import plugin.customcooking.utility.GUIUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.getServer;
import static plugin.customcooking.functions.cooking.competition.bossbar.BossBarManager.cache;

public class JadeManager extends Function {

    protected static Database database;
    public static HashMap<String, JadeSource> jadeSources = new HashMap<>();
    private static BukkitScheduler scheduler;
    private VoteListener voteListener;
    private BreweryListener breweryListener;
    public static HashMap<LeaderboardType, Leaderboard> leaderboardCache = new HashMap<>();

    public JadeManager(Database database) {
        this.database = database;
        this.voteListener = new VoteListener(this);
        this.breweryListener = new BreweryListener();
    }

    @Override
    public void load() {
        loadJadeLimits();
        Bukkit.getPluginManager().registerEvents(voteListener, CustomCooking.plugin);
        Bukkit.getPluginManager().registerEvents(breweryListener, CustomCooking.plugin);
        database.verifyAndFixTotals();
        database.startRetryTask();
        reloadLeaderboards();
        scheduler = CustomCooking.getInstance().getServer().getScheduler();
        scheduler.runTaskTimer(CustomCooking.getInstance(), new AnnoucmentRunnable(CustomCooking.getInstance()), 0L, 20L * 60 * 5);
    }

    @Override
    public void unload() {
        jadeSources.clear();
        leaderboardCache.clear();
        if (scheduler != null) {
            scheduler.cancelTasks(CustomCooking.getInstance());
        }
    }

    private void loadJadeLimits() {
        YamlConfiguration config = ConfigUtil.getConfig("config.yml");
        List<String> jadeSourceList = new ArrayList<>();
        for (String key : config.getConfigurationSection("jade.sources").getKeys(false)) {
            int limit = config.getInt("jade.sources." + key + ".limit", -1);
            long cooldown = config.getLong("jade.sources." + key + ".cooldown", 0);
            jadeSources.put(key, new JadeSource(key, cooldown, limit));
        }
        for (String source : database.getAllSources()) {
            if (!jadeSources.containsKey(source)) {
                jadeSourceList.add(source);
            }
        }
        AdventureUtil.consoleMessage("[CustomCooking] Initialised Jade limit system");
        AdventureUtil.consoleMessage("[CustomCooking] Loaded Jade limits: " + jadeSources.keySet());
        AdventureUtil.consoleMessage("[CustomCooking] Jade sources not in database: " + jadeSourceList);
    }

    public void reloadLeaderboards() {
        leaderboardCache = new HashMap<>();
        for (LeaderboardType type : LeaderboardType.values()) {
            leaderboardCache.put(type, database.queryLeaderboard(type));
        }
    }

    public Leaderboard getLeaderboard(LeaderboardType type) {
        return leaderboardCache.get(type);
    }

    public static void giveJadeCommand(Player player, String source, Integer amount) {
        // Check if source exists
        if (!jadeSources.containsKey(source) && !source.isEmpty()) {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + MessageManager.jadeSourceNotFound
                    .replace("{source}", GUIUtil.formatString(source)));
            return;
        }
        // Check if player is on cooldown
        if (!source.isEmpty() && jadeSources.get(source).getCooldown() != 0 && database.isOnCooldown(player, source)) {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + MessageManager.jadeCooldown
                    .replace("{time}", String.valueOf(database.getCooldownTimeLeft(player, source))));
            return;
        }
        // Check if player has reached Limit
        if (database.getRecentPositiveTransactionTimestamps(player, source).size() + 1 <= getLimitForSource(source)) {
            give(player, amount, source);
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + MessageManager.jadeLimitReached
                    .replace("{source}", GUIUtil.formatString(source)));
            sendJadeLimitMessage(player);
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

        database.addTransaction(new JadeTransaction(player.getName().toLowerCase(), player.getUniqueId(), amount, source, LocalDateTime.now()));

        JadeEvent jadeEvent = new JadeEvent(player, amount, source);
        Bukkit.getPluginManager().callEvent(jadeEvent);

        String bcast = MessageManager.infoPositive + MessageManager.jadeBroadcast
                .replace("{source}", source.isEmpty() ? "playing" : GUIUtil.formatString(source))
                .replace("{player}", player.getName())
                .replace("{amount}", String.valueOf((int) amount));
        getServer().broadcast(AdventureUtil.getComponentFromMiniMessage(bcast));
    }

    public static void giveOffline(OfflinePlayer player, double amount, String source) {
        database.addTransaction(new JadeTransaction(player.getName().toLowerCase(), player.getUniqueId(), amount, source, LocalDateTime.now()));
    }

    public static void remove(Player player, double amount, String source) {
        database.addTransaction(new JadeTransaction(player.getName().toLowerCase(), player.getUniqueId(), -amount, source, LocalDateTime.now()));
    }

    public static int getLimitForSource(String source) {
        if (source.isEmpty() || jadeSources.get(source) == null) {
            return Integer.MAX_VALUE;
        }
        return jadeSources.get(source).getLimit();
    }

    public static String checkJadeLimit(Player player, String source) {
        return database.getRecentPositiveTransactionTimestamps(player, source).size() + "/" + getLimitForSource(source);
    }

    public static int getTotalJadeForPlayer(Player player) {
        return database.getJadeForPlayer(player);
    }

    public static int sendJadeLimitMessage(Player player) {
        HashMap<String, Double> jadeData = database.getJadeFromSources(player);
        if (jadeData.size() == 1 && jadeData.containsKey("not_in_database")) {
            return -1;
        }
        StringBuilder message = new StringBuilder();
        message.append(MessageManager.jadeLimitHeader);
        for (String source : jadeSources.keySet()) {
            int limit = JadeManager.getLimitForSource(source);
            if (limit == -1) {
                continue;
            }
            Double total = jadeData.getOrDefault(source, 0.0);
            String sourceMessage = MessageManager.jadeLimitSource
                    .replace("{source}", GUIUtil.formatString(source))
                    .replace("{total}", String.valueOf(total.intValue()))
                    .replace("{limit}", String.valueOf(limit));
            message.append(sourceMessage);
        }
        message.append(MessageManager.jadeLimitFooter);
        AdventureUtil.sendMessage(player, message.toString());
        return jadeData.size();
    }

    public static void reconsileJadeData() {
        for (VotingPluginUser user : database.getAllTotals()) {
            String name = user.getPlayerName().toLowerCase();
            String uuid = user.getUUID().toString();
            int avPoints = user.getPoints();
            if (avPoints == 0) {
                continue;
            }
            double jade = database.getTotalJadeByUUID(uuid);
                if (avPoints > jade) {
                    int diff = (int) (avPoints - jade);
                    giveOffline(Bukkit.getOfflinePlayer(uuid), diff, "");
                    user.setPoints(0);
                    System.out.println("Reconciled " + diff + " jade for " + name);
                } else {
                    System.out.println("No reconciliation needed for " + name);
                }
        }
    }
}