package plugin.borealcore.jade;

import com.bencodez.votingplugin.VotingPluginHooks;
import com.bencodez.votingplugin.user.VotingPluginUser;
import com.dre.brewery.Brew;
import com.dre.brewery.api.events.brew.BrewModifyEvent;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.momirealms.customcrops.api.core.mechanic.crop.CropConfig;
import net.momirealms.customcrops.api.event.CropBreakEvent;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import plugin.borealcore.BorealCore;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.api.module.ModuleLoadException;
import plugin.borealcore.jade.config.JadeConfigLoader;
import plugin.borealcore.jade.config.JadeDatabase;
import plugin.borealcore.jade.config.JadeMessage;
import plugin.borealcore.jade.config.JadeMessageLoader;
import plugin.borealcore.jade.object.JadeEvent;
import plugin.borealcore.jade.object.JadePapi;
import plugin.borealcore.jade.object.JadeSource;
import plugin.borealcore.jade.object.JadeTransaction;
import plugin.borealcore.manager.ConfigManager;
import plugin.borealcore.object.DebugLevel;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.GuiUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.getServer;
import static plugin.borealcore.jade.config.JadeConfig.brewingRequiredQuality;
import static plugin.borealcore.jade.config.JadeConfig.refarmableCrops;

public class JadeModule implements BorealModule {

    protected static JadeDatabase database;
    public static HashMap<String, JadeSource> jadeSources = new HashMap<>();
    private final JadeSourceListener jadeSourceListener;
    private JadePapi jadePlaceholders;
    private ModuleContext context;
    private BukkitTask announcementTask;

    public JadeModule() {
        this.jadeSourceListener = new JadeSourceListener(this);
    }

    public static JadeDatabase getDatabase() {
        return database;
    }

    @Override
    public void onModuleEnable() throws Exception {
        JadeConfigLoader.load();
        JadeMessageLoader.load();

        loadJadeLimits();
        Bukkit.getPluginManager().registerEvents(jadeSourceListener, BorealCore.plugin);
        this.jadePlaceholders = new JadePapi();
        context.getPlaceholderManager().registerExpansion(jadePlaceholders);
        announcementTask = context.getPlugin().getServer().getScheduler().runTaskTimer(context.getPlugin(), new AnnoucmentRunnable(context.getPlugin()), 0L, 20L * 60 * 15);
    }

    @Override
    public void onModuleDisable() throws Exception {
        jadeSources.clear();
        JadeDatabase.leaderboardCache.clear();
        if (this.jadeSourceListener != null) HandlerList.unregisterAll(this.jadeSourceListener);
        if (this.announcementTask != null && !this.announcementTask.isCancelled()) this.announcementTask.cancel();
        if (this.jadePlaceholders != null) context.getPlaceholderManager().unregisterExpansion(jadePlaceholders);
    }

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {
        this.context = context;
        database = new JadeDatabase(context.getDatabaseManager());

        context.registerCommand(new JadeCommand(this).buildCommandNode());

        if (!database.load(context)) {
            throw new ModuleLoadException("Failed to load Jade database");
        }
    }

    private void loadJadeLimits() {
        YamlConfiguration config = ConfigManager.getConfig("config.yml");
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

    public static void giveJadeCommand(Player player, String source, Integer amount) {
        // Check if source exists
        if (!jadeSources.containsKey(source) && !source.isEmpty()) {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + JadeMessage.jadeSourceNotFound
                    .replace("{source}", GuiUtil.formatString(source)));
            return;
        }
        // Check if player is on cooldown
        if (!source.isEmpty() && jadeSources.get(source).getCooldown() != 0 && database.isOnCooldown(player, source)) {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + JadeMessage.jadeCooldown
                    .replace("{time}", String.valueOf(database.getCooldownTimeLeft(player, source))));
            return;
        }
        // Check if player has reached Limit
        if (database.getRecentPositiveTransactionTimestamps(player, source).size() + 1 <= getLimitForSource(source)) {
            give(player, amount, source);
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + JadeMessage.jadeLimitReached
                    .replace("{source}", GuiUtil.formatString(source)));
            sendJadeLimitMessage(player);
        }
    }

    public static void setBalance(Player player, int amount, String source) {
        int currentBalance = database.getJadeForPlayer(player);
        int diff = amount - currentBalance;
        if (diff > 0) {
            give(player, diff, source);
        } else if (diff < 0) {
            remove(player, -diff, source);
        }
    }

    public static void give(Player player, double amount, String source) {
        boolean first = source.isBlank() || database.getRecentPositiveTransactionTimestamps(player, source).isEmpty();
        if (first && !source.isEmpty()) {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + JadeMessage.jadeFirstTime
                    .replace("{source}", GuiUtil.formatString(source))
                    .replace("{limit}", String.valueOf(getLimitForSource(source))));
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + JadeMessage.jadeReceived
                    .replace("{amount}", String.valueOf(amount)));
        }

        database.addTransaction(new JadeTransaction(player.getName().toLowerCase(), player.getUniqueId(), amount, source, LocalDateTime.now()));

        JadeEvent jadeEvent = new JadeEvent(player, amount, source);
        Bukkit.getPluginManager().callEvent(jadeEvent);

        String bcast = MessageManager.infoPositive + JadeMessage.jadeBroadcast
                .replace("{source}", source.isEmpty() ? "playing" : GuiUtil.formatString(source))
                .replace("{player}", player.getName())
                .replace("{amount}", String.valueOf((int) amount));
        getServer().getOnlinePlayers().stream().filter(p -> !p.hasPermission("jade.notifications")).forEach(p -> AdventureUtil.sendMessage(p, bcast));
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
        if (jadeData.containsKey("not_in_last_24_hours")) {
            return 0;
        }
        StringBuilder message = new StringBuilder();
        message.append(JadeMessage.jadeLimitHeader);
        for (String source : jadeSources.keySet()) {
            int limit = JadeModule.getLimitForSource(source);
            if (limit == -1) {
                continue;
            }
            Double total = jadeData.getOrDefault(source, 0.0);
            String sourceMessage = JadeMessage.jadeLimitSource
                    .replace("{source}", GuiUtil.formatString(source))
                    .replace("{total}", String.valueOf(total.intValue()))
                    .replace("{limit}", String.valueOf(limit));
            message.append(sourceMessage);
        }
        message.append(JadeMessage.jadeLimitFooter);
        AdventureUtil.sendMessage(player, message.toString());
        return jadeData.size();
    }

    public static void reconcileJadeData(Player player) {
        VotingPluginUser user = VotingPluginHooks.getInstance().getUserManager().getVotingPluginUser(player);
        if (user == null) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "User not found for " + player.getName());
            return;
        }
        int avPoints = user.getPoints();
        if (avPoints == 0) {
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Jade data is already reconciled for " + player.getName());
            return;
        }
        database.getJadeForPlayerAsync(player, currentJade -> {
            if (avPoints > currentJade) {
                int diff = avPoints - currentJade;
                give(player, diff, "");
                user.setPoints(0);
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Reconciled " + diff + " jade for " + player.getName());
            } else {
                AdventureUtil.consoleMessage(DebugLevel.DEBUG, "No reconciliation needed for " + player.getName());
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
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, String.format(
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
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Fishing result: " + event.getResult() + ", player: " + event.getPlayer().getName() + ", loot: " + event.getLoot() + " group: " + event.getLoot().lootGroup().toString());
        if (event.getResult().equals(FishingResultEvent.Result.SUCCESS) && Math.random() <= jadeSources.get("fishing").getRate()) {
            giveJadeCommand(event.getPlayer(), "fishing", 1);
        }
    }

    public void farmingJade(CropBreakEvent event) {
        if (event.entityBreaker() instanceof Player player) {
            CropConfig cropConfig = event.cropConfig();
            AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Processing farmingJade for player: " + player.getName() +
                    ", crop: " + event.cropStageItemID() + ", reason: " + event.reason());
            if (refarmableCrops.contains(cropConfig.id()) ? Math.random() <= jadeSources.get("farming").getRate() * 0.5 : Math.random() <= jadeSources.get("farming").getRate()) {
                giveJadeCommand(player, "farming", 1);
            }
        }
    }

    public static void cookingJade(Player player) {
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Processing cookingJade for player: " + player.getName());
        if (Math.random() <= jadeSources.get("cooking").getRate()) {
            giveJadeCommand(player, "cooking", 1);
        }
    }
}