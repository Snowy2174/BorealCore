package plugin.customcooking.cooking.competition.bossbar;

import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.cooking.competition.Competition;
import plugin.customcooking.cooking.competition.ranking.RankingInterface;
import plugin.customcooking.manager.PlaceholderManager;
import plugin.customcooking.manager.configs.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TextCache {

    private final Player owner;
    private String originalValue;
    private String latestValue;
    private String[] ownerPlaceholders;

    public TextCache(Player owner, String rawValue) {
        this.owner = owner;
        analyze(rawValue);
    }

    private void analyze(String value) {
        List<String> placeholdersOwner = new ArrayList<>(CustomCooking.plugin.getPlaceholderManager().detectPlaceholders(value));
        String origin = value;
        for (String placeholder : placeholdersOwner) {
            origin = origin.replace(placeholder, "%s");
        }
        originalValue = origin;
        ownerPlaceholders = placeholdersOwner.toArray(new String[0]);
        latestValue = originalValue;
        update();
    }

    public String getLatestValue() {
        return latestValue;
    }

    public boolean update() {
        String string = originalValue;
        if (ownerPlaceholders.length != 0) {
            PlaceholderManager placeholderManager = CustomCooking.plugin.getPlaceholderManager();
            if ("%s".equals(originalValue)) {
                string = placeholderManager.parse(owner, ownerPlaceholders[0]);
            }
            else {
                Object[] values = new String[ownerPlaceholders.length];
                for (int i = 0; i < ownerPlaceholders.length; i++) {
                    values[i] = placeholderManager.parse(owner, ownerPlaceholders[i]);
                }
                string = String.format(originalValue, values);
            }
        }

        RankingInterface ranking = Competition.currentCompetition.getRanking();

        string = string.replace("{rank}", Competition.currentCompetition.getPlayerRank(owner))
                .replace("{time}", String.valueOf(Competition.currentCompetition.getRemainingTime()))
                .replace("{minute}", String.format("%02d", Competition.currentCompetition.getRemainingTime() / 60))
                .replace("{second}",String.format("%02d", Competition.currentCompetition.getRemainingTime() % 60))
                .replace("{score}", String.format("%.1f", Competition.currentCompetition.getScore(owner)))
                .replace("{1st_player}", Optional.ofNullable(ranking.getPlayerAt(1)).orElse(MessageManager.noPlayer))
                .replace("{1st_score}", ranking.getScoreAt(1) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(1)))
                .replace("{2nd_player}", Optional.ofNullable(ranking.getPlayerAt(2)).orElse(MessageManager.noPlayer))
                .replace("{2nd_score}", ranking.getScoreAt(2) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(2)))
                .replace("{3rd_player}", Optional.ofNullable(ranking.getPlayerAt(3)).orElse(MessageManager.noPlayer))
                .replace("{3rd_score}", ranking.getScoreAt(3) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(3)));

        if (!latestValue.equals(string)) {
            latestValue = string;
            return true;
        }
        return false;
    }
}
