package plugin.borealcore.functions.cooking.competition;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.CookingCompetitionManager;
import plugin.borealcore.object.Function;

import java.time.LocalTime;

public class CompetitionSchedule extends Function {

    @Override
    public void load() {
        checkTime();
    }

    @Override
    public void unload() {
        stopCheck();
        cancelCompetition();
    }

    private BukkitTask checkTimeTask;
    private int doubleCheckTime;

    public static boolean startCompetition(String competitionName) {
        CompetitionConfig competitionConfig = CookingCompetitionManager.competitionsC.get(competitionName);
        if (competitionConfig == null) return false;
        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.end();
        }
        Competition.currentCompetition = new Competition(competitionConfig);
        Competition.currentCompetition.begin(true);
        return true;
    }

    public static void cancelCompetition() {
        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.cancel();
        }
    }

    public static void endCompetition() {
        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.end();
        }
    }

    public void startCompetition(CompetitionConfig competitionConfig) {
        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.end();
        }
        Competition.currentCompetition = new Competition(competitionConfig);
        Competition.currentCompetition.begin(false);
    }

    public void checkTime() {
        this.checkTimeTask = new BukkitRunnable() {
            public void run() {
                if (isANewMinute()) {
                    CompetitionConfig competitionConfig = CookingCompetitionManager.competitionsT.get(getCurrentTime());
                    if (competitionConfig != null && competitionConfig.canStart()) {
                        startCompetition(competitionConfig);
                    }
                }
            }
        }.runTaskTimer(BorealCore.plugin, 60 - LocalTime.now().getSecond(), 100);
    }

    public void stopCheck() {
        if (this.checkTimeTask != null) {
            checkTimeTask.cancel();
        }
    }

    public String getCurrentTime() {
        return LocalTime.now().getHour() + ":" + String.format("%02d", LocalTime.now().getMinute());
    }

    private boolean isANewMinute() {
        int minute = LocalTime.now().getMinute();
        if (doubleCheckTime != minute) {
            doubleCheckTime = minute;
            return true;
        } else {
            return false;
        }
    }
}
