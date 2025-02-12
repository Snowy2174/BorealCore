package plugin.customcooking.functions.cooking;

import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.YamlConfiguration;
import plugin.customcooking.functions.cooking.action.Action;
import plugin.customcooking.functions.cooking.action.CommandActionImpl;
import plugin.customcooking.functions.cooking.action.MessageActionImpl;
import plugin.customcooking.functions.cooking.competition.CompetitionConfig;
import plugin.customcooking.functions.cooking.competition.CompetitionGoal;
import plugin.customcooking.functions.cooking.competition.CompetitionSchedule;
import plugin.customcooking.functions.cooking.competition.bossbar.BossBarConfig;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;
import plugin.customcooking.utility.ConfigUtil;

import java.util.*;


public class CompetitionManager extends Function {

    public static HashMap<String, CompetitionConfig> competitionsT;
    public static HashMap<String, CompetitionConfig> competitionsC;
    private CompetitionSchedule competitionSchedule;

    @Override
    public void load() {
        competitionsC = new HashMap<>();
        competitionsT = new HashMap<>();
        loadCompetitions();
        this.competitionSchedule = new CompetitionSchedule();
        this.competitionSchedule.load();
    }

    @Override
    public void unload() {
        if (competitionsC != null) competitionsC.clear();
        if (competitionsT != null) competitionsT.clear();
        if (competitionSchedule != null) competitionSchedule.unload();
    }

    public void loadCompetitions() {
        YamlConfiguration config = ConfigUtil.getConfig("competition.yml");
        Set<String> keys = config.getKeys(false);
        keys.forEach(key -> {
            boolean enableBsb = config.getBoolean(key + ".bossbar.enable", false);
            BossBarConfig bossBarConfig = new BossBarConfig(
                    config.getStringList(key + ".bossbar.text").toArray(new String[0]),
                    BossBarConfig.BossBarOverlay.valueOf(config.getString(key + ".bossbar.overlay", "SOLID").toUpperCase()),
                    BarColor.valueOf(config.getString(key + ".bossbar.color", "WHITE").toUpperCase()),
                    config.getInt(key + ".bossbar.refresh-rate", 10),
                    config.getInt(key + ".bossbar.switch-interval", 200)
            );

            HashMap<String, Action[]> rewardsMap = new HashMap<>();
            Objects.requireNonNull(config.getConfigurationSection(key + ".prize")).getKeys(false).forEach(rank -> {
                List<Action> rewards = new ArrayList<>();
                if (config.contains(key + ".prize." + rank + ".messages"))
                    rewards.add(new MessageActionImpl(config.getStringList(key + ".prize." + rank + ".messages").toArray(new String[0]), null));
                if (config.contains(key + ".prize." + rank + ".commands"))
                    rewards.add(new CommandActionImpl(config.getStringList(key + ".prize." + rank + ".commands").toArray(new String[0]), null));
                rewardsMap.put(rank, rewards.toArray(new Action[0]));
            });

            CompetitionConfig competitionConfig = new CompetitionConfig(
                    config.getInt(key + ".duration", 600),
                    config.getInt(key + ".min-players", 1),
                    config.getStringList(key + ".broadcast.start"),
                    config.getStringList(key + ".broadcast.end"),
                    config.getStringList(key + ".command.start"),
                    config.getStringList(key + ".command.end"),
                    config.getStringList(key + ".command.join"),
                    CompetitionGoal.valueOf(config.getString(key + ".goal", "RANDOM")),
                    bossBarConfig,
                    enableBsb,
                    rewardsMap
            );

            if (config.contains(key + ".start-weekday")) {
                List<Integer> days = new ArrayList<>();
                for (String weekDay : config.getStringList(key + ".start-weekday")) {
                    switch (weekDay) {
                        case "Sunday" -> days.add(1);
                        case "Monday" -> days.add(2);
                        case "Tuesday" -> days.add(3);
                        case "Wednesday" -> days.add(4);
                        case "Thursday" -> days.add(5);
                        case "Friday" -> days.add(6);
                        case "Saturday" -> days.add(7);
                        default -> AdventureUtil.consoleMessage("unknown weekday: " + weekDay);
                    }
                }
                competitionConfig.setWeekday(days);
            }

            if (config.contains(key + ".start-date")) {
                List<Integer> days = new ArrayList<>();
                for (String weekDay : config.getStringList(key + ".start-date")) {
                    days.add(Integer.parseInt(weekDay));
                }
                competitionConfig.setDate(days);
            }

            config.getStringList(key + ".start-time").forEach(time -> competitionsT.put(time, competitionConfig));
            competitionsC.put(key, competitionConfig);
        });
    }
}
