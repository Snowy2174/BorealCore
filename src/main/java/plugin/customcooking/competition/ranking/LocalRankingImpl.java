package plugin.customcooking.competition.ranking;

import plugin.customcooking.competition.CompetitionPlayer;

import java.util.*;

public class LocalRankingImpl implements RankingInterface {

    private final Set<CompetitionPlayer> competitionPlayers = Collections.synchronizedSet(new TreeSet<>());

    public void addPlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.add(competitionPlayer);
    }

    public void removePlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.removeIf(e -> e == competitionPlayer);
    }

    @Override
    public void clear() {
        competitionPlayers.clear();
    }

    @Override
    public CompetitionPlayer getCompetitionPlayer(String player) {
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return competitionPlayer;
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getIterator() {
        List<String> players = new ArrayList<>();
        for (CompetitionPlayer competitionPlayer: competitionPlayers){
            players.add(competitionPlayer.getPlayer());
        }
        return players.iterator();
    }

    @Override
    public int getSize() {
        return competitionPlayers.size();
    }

    @Override
    public String getPlayerRank(String player) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return String.valueOf(index);
            }else {
                index++;
            }
        }
        return null;
    }

    @Override
    public float getPlayerScore(String player) {
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return competitionPlayer.getScore();
            }
        }
        return 0;
    }

    @Override
    public String getPlayerAt(int i) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (index == i) {
                return competitionPlayer.getPlayer();
            }
            index++;
        }
        return null;
    }

    @Override
    public float getScoreAt(int i) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (index == i) {
                return competitionPlayer.getScore();
            }
            index++;
        }
        return 0f;
    }

    @Override
    public void refreshData(String player, float score) {
        CompetitionPlayer competitionPlayer = getCompetitionPlayer(player);
        if (competitionPlayer != null) {
            removePlayer(competitionPlayer);
            competitionPlayer.addScore(score);
            addPlayer(competitionPlayer);
        } else {
            competitionPlayer = new CompetitionPlayer(player, score);
            addPlayer(competitionPlayer);
        }
    }

    @Override
    public void setData(String player, float score) {
        CompetitionPlayer competitionPlayer = getCompetitionPlayer(player);
        if (competitionPlayer != null) {
            removePlayer(competitionPlayer);
            competitionPlayer.setScore(score);
            addPlayer(competitionPlayer);
        } else {
            competitionPlayer = new CompetitionPlayer(player, score);
            addPlayer(competitionPlayer);
        }
    }
}
