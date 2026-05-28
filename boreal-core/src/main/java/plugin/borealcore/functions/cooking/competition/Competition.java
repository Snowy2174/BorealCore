package plugin.borealcore.functions.cooking.competition;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plugin.borealcore.BorealCore;
import plugin.borealcore.action.Action;
import plugin.borealcore.functions.cooking.competition.bossbar.BossBarManager;
import plugin.borealcore.functions.cooking.competition.ranking.LocalRankingImpl;
import plugin.borealcore.functions.cooking.competition.ranking.RankingInterface;
import plugin.borealcore.manager.PlaceholderManager;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.RecipeDataUtil;

import java.time.Instant;
import java.util.*;

public class Competition {

    public static Competition currentCompetition;

    private final CompetitionConfig competitionConfig;
    private CompetitionGoal goal;
    private BukkitTask timerTask;
    private RankingInterface ranking;
    private long startTime;
    private long remainingTime;
    private float progress;
    private BossBarManager bossBarManager;

    public Competition(CompetitionConfig competitionConfig) {
        this.competitionConfig = competitionConfig;
    }

    public static boolean hasCompetitionOn() {
        return currentCompetition != null;
    }

    public static Competition getCurrentCompetition() {
        return currentCompetition;
    }

    public void begin(boolean forceStart) {
        this.goal = competitionConfig.getGoal();
        if (this.goal == CompetitionGoal.RANDOM) {
            this.goal = getRandomGoal();
        }
        this.remainingTime = this.competitionConfig.getDuration();
        this.startTime = Instant.now().getEpochSecond();

        Collection<? extends Player> playerCollections = Bukkit.getOnlinePlayers();
        List<Player> validPlayers = new ArrayList<>();

        for (Player player : playerCollections) {
            if (RecipeDataUtil.playerDataExists(player)) {
                validPlayers.add(player);
            }
        }

        if (validPlayers.size() >= competitionConfig.getMinPlayers() || forceStart) {

            ranking = new LocalRankingImpl();
            startTimer();
            for (String startMsg : competitionConfig.getStartMessage()) {
                for (Player player : playerCollections) {
                    AdventureUtil.playerMessage(player, startMsg);
                }
            }
            for (String startCmd : competitionConfig.getStartCommand()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), startCmd);
            }
            if (competitionConfig.isEnableBossBar()) {
                bossBarManager = new BossBarManager();
                bossBarManager.load();
            }
        } else {
            for (Player player : playerCollections) {
                AdventureUtil.playerMessage(player, MessageManager.prefix + MessageManager.notEnoughPlayers);
            }
            currentCompetition = null;
        }
    }

    private void startTimer() {
        this.timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (decreaseTime()) {
                    end();
                }
            }
        }.runTaskTimer(BorealCore.plugin, 0, 20);
    }

    public void cancel() {
        if (this.bossBarManager != null) {
            bossBarManager.unload();
        }
        ranking.clear();
        this.timerTask.cancel();
        currentCompetition = null;
    }

    public void end() {
        if (this.bossBarManager != null) {
            bossBarManager.unload();
        }
        this.timerTask.cancel();

        List<String> newMessage = new ArrayList<>();
        PlaceholderManager placeholderManager = BorealCore.getPlaceholderManager();

        for (String endMsg : competitionConfig.getEndMessage()) {
            List<String> placeholders = new ArrayList<>(placeholderManager.detectPlaceholders(endMsg));
            for (String placeholder : placeholders) {
                if (placeholder.endsWith("_player%")) {
                    int rank = Integer.parseInt(placeholder.substring(1, placeholder.length() - 8));
                    endMsg = endMsg.replace(placeholder, Optional.ofNullable(ranking.getPlayerAt(rank)).orElse(MessageManager.noPlayer));
                } else if (placeholder.endsWith("_score%")) {
                    int rank = Integer.parseInt(placeholder.substring(1, placeholder.length() - 7));
                    float score = ranking.getScoreAt(rank);
                    endMsg = endMsg.replace(placeholder, score == 0 ? MessageManager.noScore : String.format("%.1f", score));
                }
            }
            newMessage.add(endMsg);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String msg : newMessage) {
                AdventureUtil.playerMessage(player, msg);
            }
        }

        for (String endCmd : competitionConfig.getEndCommand()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), endCmd);
        }

        givePrize();

        currentCompetition = null;

        Bukkit.getScheduler().runTaskLaterAsynchronously(BorealCore.plugin, () -> {
            ranking.clear();
        }, 600);
    }

    public void givePrize() {
        HashMap<String, Action[]> rewardsMap = competitionConfig.getRewards();
        if (ranking.getSize() != 0 && rewardsMap != null) {
            Iterator<String> iterator = ranking.getIterator();
            int i = 1;
            while (iterator.hasNext()) {
                if (i < rewardsMap.size()) {
                    String playerName = iterator.next();
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        for (Action action : rewardsMap.get(String.valueOf(i))) {
                            action.doOn(player, null);
                        }
                    }
                    i++;
                } else {
                    Action[] actions = rewardsMap.get("participation");
                    if (actions != null) {
                        iterator.forEachRemaining(playerName -> {
                            Player player = Bukkit.getPlayer(playerName);
                            if (player != null) {
                                for (Action action : actions) {
                                    action.doOn(player, null);
                                }
                            }
                        });
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private boolean decreaseTime() {

        long tVac;
        long current = Instant.now().getEpochSecond();
        int duration = competitionConfig.getDuration();

        progress = (float) remainingTime / duration;

        remainingTime = duration - (current - startTime);
        if ((tVac = (current - startTime) + 1) != duration - remainingTime) {
            for (long i = duration - remainingTime; i < tVac; i++) {
                if (remainingTime <= 0) return true;
                remainingTime--;
            }
        }
        return false;
    }

    private CompetitionGoal getRandomGoal() {
        return CompetitionGoal.values()[new Random().nextInt(CompetitionGoal.values().length - 1)];
    }

    public float getProgress() {
        return progress;
    }

    public CompetitionConfig getCompetitionConfig() {
        return competitionConfig;
    }

    public String getPlayerRank(Player player) {
        return Optional.ofNullable(ranking.getPlayerRank(player.getName())).orElse(MessageManager.noRank);
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public double getScore(Player player) {
        return Optional.ofNullable(ranking.getCompetitionPlayer(player.getName())).orElse(CompetitionPlayer.emptyPlayer).getScore();
    }

    public boolean isJoined(Player player) {
        return ranking.getCompetitionPlayer(player.getName()) != null;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public void tryAddBossBarToPlayer(Player player) {
        if (bossBarManager != null) {
            bossBarManager.tryJoin(player);
        }
    }

    public void refreshData(Player player, float score, boolean doubleScore) {
        if (this.goal == CompetitionGoal.CATCH_AMOUNT) {
            score = 1f;
        }
        ranking.refreshData(player.getName(), doubleScore ? 2 * score : score);
    }

    public CompetitionGoal getGoal() {
        return goal;
    }

    public RankingInterface getRanking() {
        return ranking;
    }

    public long getStartTime() {
        return startTime;
    }
}
