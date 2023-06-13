package plugin.customcooking.cooking.competition.ranking;

import org.jetbrains.annotations.Nullable;
import plugin.customcooking.cooking.competition.CompetitionPlayer;

import java.util.Iterator;

public interface RankingInterface {

    void clear();
    CompetitionPlayer getCompetitionPlayer(String player);
    Iterator<String> getIterator();
    int getSize();
    String getPlayerRank(String player);
    float getPlayerScore(String player);
    void refreshData(String player, float score);
    void setData(String player, float score);
    @Nullable
    String getPlayerAt(int rank);
    float getScoreAt(int rank);
}
