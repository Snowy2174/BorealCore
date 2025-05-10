package plugin.customcooking.functions.jade;

import java.util.List;
import java.util.stream.Collectors;

public class Leaderboard {
    private LeaderboardType type;
    private List<LeaderboardEntry> entries;

    public Leaderboard(LeaderboardType type, List<LeaderboardEntry> entries) {
        this.type = type;
        this.entries = entries;
    }

    public List<LeaderboardEntry> getTop(int n) {
        return entries.stream().limit(n).collect(Collectors.toList());
    }
}
