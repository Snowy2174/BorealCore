package plugin.borealcore.functions.jade;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.user.VotingPluginUser;
import com.dre.brewery.Brew;
import com.dre.brewery.api.events.brew.BrewModifyEvent;
import net.momirealms.customcrops.api.core.mechanic.crop.CropConfig;
import net.momirealms.customcrops.api.event.CropBreakEvent;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import plugin.borealcore.BorealCore;
import plugin.borealcore.api.event.JadeEvent;
import plugin.borealcore.database.Database;
import plugin.borealcore.functions.jade.object.JadeSource;
import plugin.borealcore.functions.jade.object.JadeTransaction;
import plugin.borealcore.functions.jade.object.Leaderboard;
import plugin.borealcore.listener.JadeSourceListener;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.ConfigUtil;
import plugin.borealcore.utility.GUIUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.getServer;
import static plugin.borealcore.manager.configs.ConfigManager.brewingRequiredQuality;
import static plugin.borealcore.manager.configs.ConfigManager.refarmableCrops;

public class JadeManager extends Function {

    protected static Database database;
    public static HashMap<String, JadeSource> jadeSources = new HashMap<>();
    private static BukkitScheduler scheduler;
    private JadeSourceListener jadeSourceListener;
    public static HashMap<LeaderboardType, Leaderboard> leaderboardCache = new HashMap<>();

    public JadeManager(Database database) {
        this.database = database;
        this.jadeSourceListener = new JadeSourceListener(this);
    }

    @Override
    public void load() {
        loadJadeLimits();
        Bukkit.getPluginManager().registerEvents(jadeSourceListener, BorealCore.plugin);
        database.verifyAndFixTotals();
        database.startRetryTask();
        reloadLeaderboards();
        scheduler = BorealCore.getInstance().getServer().getScheduler();
        scheduler.runTaskTimer(BorealCore.getInstance(), new AnnoucmentRunnable(BorealCore.getInstance()), 0L, 20L * 60 * 15);
    }

    @Override
    public void unload() {
        jadeSources.clear();
        leaderboardCache.clear();

        if (scheduler != null) {
            scheduler.cancelTasks(BorealCore.getInstance());
        }
    }

    private void loadJadeLimits() {
        YamlConfiguration config = ConfigUtil.getConfig("config.yml");
        List<String> jadeSourceList = new ArrayList<>();
        for (String key : config.getConfigurationSection("jade.sources").getKeys(false)) {
            int limit = config.getInt("jade.sources." + key + ".limit", -1);
            long cooldown = config.getLong("jade.sources." + key + ".cooldown", 0);
            double rate = config.getDouble("jade.sources." + key + ".rate", 1.0);
            jadeSources.put(key, new JadeSource(key, cooldown, limit, rate));
        }
        for (String source : database.getAllSources()) {
            if (!jadeSources.containsKey(source)) {
                jadeSourceList.add(source);
            }
        }
        AdventureUtil.consoleMessage("Initialised Jade limit system");
        AdventureUtil.consoleMessage("Loaded Jade limits: " + jadeSources.keySet());
        AdventureUtil.consoleMessage("Jade sources not in database: " + jadeSourceList);
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

    public static void reconsileJadeData(Player player) {
        VotingPluginUser user = VotingPluginHooks.getInstance().getUserManager().getVotingPluginUser(player);
        if (user == null) {
            System.out.println("User not found for " + player.getName());
            return;
        }
        int avPoints = user.getPoints();
        if (avPoints == 0) {
            System.out.println("Jade data is already reconciled for " + player.getName());
            return;
        }
        database.getJadeForPlayerAsync(player, currentJade -> {
            if (avPoints > currentJade) {
                int diff = avPoints - currentJade;
                give(player, diff, "");
                user.setPoints(0);
                System.out.println("Reconciled " + diff + " jade for " + player.getName());
            } else {
                System.out.println("No reconciliation needed for " + player.getName());
            }
        });
    }

    public void breweryJade(BrewModifyEvent event) {
        Player player = event.getPlayer();
        Brew brew = event.getBrew();
        int quality = brew.getQuality();
        int age = brew.getCurrentRecipe().getAge();
        boolean distilled = brew.getCurrentRecipe().getDistillTime() > 1;
        double brewingRate = jadeSources.get("brewing").getRate();
        BorealCore.getInstance().getLogger().info(String.format(
                "Processing breweryJade for player: %s, quality: %d, brew: %s, age: %d, distilled: %b",
                player.getName(), quality, Arrays.toString(brew.getCurrentRecipe().getName()), age, distilled
        ));
        if (quality >= brewingRequiredQuality) {
            boolean agedCondition = age > 1 && Math.random() <= brewingRate;
            boolean distilledCondition = distilled && Math.random() <= (brewingRate * 0.5);
            if (agedCondition || distilledCondition) {
                giveJadeCommand(player, "brewing", 1);
            }
        }
    }

    public static void fishingJade(FishingResultEvent event) {
        System.out.println("Fishing result: " + event.getResult() + ", player: " + event.getPlayer().getName() + ", loot: " + event.getLoot() + " group: " + event.getLoot().lootGroup().toString());
        if (event.getResult().equals(FishingResultEvent.Result.SUCCESS) && Math.random() <= jadeSources.get("fishing").getRate()) {
            giveJadeCommand(event.getPlayer(), "fishing", 1);
        }
    }

    public void farmingJade(CropBreakEvent event) {
        if (event.entityBreaker() instanceof Player) {
            Player player = (Player) event.entityBreaker();
            CropConfig cropConfig = event.cropConfig();
            BorealCore.getInstance().getLogger().info("Processing farmingJade for player: " + player.getName() +
                    ", crop: " + event.cropStageItemID() + ", reason: " + event.reason());
            if (refarmableCrops.contains(cropConfig.id()) ? Math.random() <= jadeSources.get("farming").getRate() * 0.5 : Math.random() <= jadeSources.get("farming").getRate()) {
                giveJadeCommand(player, "farming", 1);
            }
        }
    }

    public static void cookingJade(Player player) {
        BorealCore.getInstance().getLogger().info("Processing cookingJade for player: " + player.getName());
        if (Math.random() <= jadeSources.get("cooking").getRate()) {
            giveJadeCommand(player, "cooking", 1);
        }
    }
}