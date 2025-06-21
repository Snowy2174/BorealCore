package plugin.borealcore.functions.cooking.competition;

import plugin.borealcore.action.Action;
import plugin.borealcore.functions.cooking.competition.bossbar.BossBarConfig;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CompetitionConfig {

    private final int duration;
    private final int minPlayers;
    private final List<String> startMessage;
    private final List<String> endMessage;
    private final List<String> startCommand;
    private final List<String> endCommand;
    private final List<String> joinCommand;
    private final CompetitionGoal goal;
    private final BossBarConfig bossBarConfig;
    private final boolean enableBossBar;
    private final HashMap<String, Action[]> rewards;
    private List<Integer> date;
    private List<Integer> weekday;

    public CompetitionConfig(int duration, int minPlayers, List<String> startMessage, List<String> endMessage, List<String> startCommand, List<String> endCommand, List<String> joinCommand, CompetitionGoal goal, BossBarConfig bossBarConfig, boolean enableBossBar, HashMap<String, Action[]> rewards) {
        this.duration = duration;
        this.minPlayers = minPlayers;
        this.startMessage = startMessage;
        this.endMessage = endMessage;
        this.startCommand = startCommand;
        this.endCommand = endCommand;
        this.joinCommand = joinCommand;
        this.goal = goal;
        this.bossBarConfig = bossBarConfig;
        this.enableBossBar = enableBossBar;
        this.rewards = rewards;
    }

    public static void main(String[] args) {
        System.out.println(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
    }

    public int getDuration() {
        return duration;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public List<String> getStartMessage() {
        return startMessage;
    }

    public List<String> getEndMessage() {
        return endMessage;
    }

    public List<String> getStartCommand() {
        return startCommand;
    }

    public List<String> getEndCommand() {
        return endCommand;
    }

    public List<String> getJoinCommand() {
        return joinCommand;
    }

    public CompetitionGoal getGoal() {
        return goal;
    }

    public BossBarConfig getBossBarConfig() {
        return bossBarConfig;
    }

    public boolean isEnableBossBar() {
        return enableBossBar;
    }

    public HashMap<String, Action[]> getRewards() {
        return rewards;
    }

    public void setDate(List<Integer> date) {
        this.date = date;
    }

    public void setWeekday(List<Integer> weekday) {
        this.weekday = weekday;
    }

    public boolean canStart() {
        if (date != null && date.size() != 0) {
            Calendar calendar = Calendar.getInstance();
            int dateDay = calendar.get(Calendar.DATE);
            if (!date.contains(dateDay)) {
                return false;
            }
        }
        if (weekday != null && weekday.size() != 0) {
            Calendar calendar = Calendar.getInstance();
            int dateDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekday.contains(dateDay);
        }
        return true;
    }
}
