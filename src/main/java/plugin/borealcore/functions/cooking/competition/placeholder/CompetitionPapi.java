package plugin.borealcore.functions.cooking.competition.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.borealcore.functions.cooking.competition.Competition;
import plugin.borealcore.functions.cooking.competition.ranking.RankingInterface;
import plugin.borealcore.manager.configs.MessageManager;

import javax.annotation.Nullable;
import java.util.Optional;

public class CompetitionPapi extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "competition";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SnowyOwl217";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (Competition.currentCompetition == null) return "";
        RankingInterface ranking = Competition.currentCompetition.getRanking();
        switch (params) {
            case "rank" -> {
                return Competition.currentCompetition.getPlayerRank(player);
            }
            case "score" -> {
                return String.format("%.1f", Competition.currentCompetition.getScore(player));
            }
            case "time" -> {
                return String.valueOf(Competition.currentCompetition.getRemainingTime());
            }
            case "minute" -> {
                return String.format("%02d", Competition.currentCompetition.getRemainingTime() / 60);
            }
            case "second" -> {
                return String.format("%02d", Competition.currentCompetition.getRemainingTime() % 60);
            }
            case "1st_score" -> {
                return ranking.getScoreAt(1) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(1));
            }
            case "1st_player" -> {
                return Optional.ofNullable(ranking.getPlayerAt(1)).orElse(MessageManager.noPlayer);
            }
            case "2nd_score" -> {
                return ranking.getScoreAt(2) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(2));
            }
            case "2nd_player" -> {
                return Optional.ofNullable(ranking.getPlayerAt(2)).orElse(MessageManager.noPlayer);
            }
            case "3rd_score" -> {
                return ranking.getScoreAt(3) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(3));
            }
            case "3rd_player" -> {
                return Optional.ofNullable(ranking.getPlayerAt(3)).orElse(MessageManager.noPlayer);
            }
        }
        return "null";
    }
}
