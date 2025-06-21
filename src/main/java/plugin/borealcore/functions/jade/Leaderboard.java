package plugin.borealcore.functions.jade;

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
    public LeaderboardEntry getEntry(int index) {
        if (index < 0 || index >= entries.size()) {
            return null;
        }
        return entries.get(index);
    }
    public LeaderboardType getType() {
        return type;
    }
    public List<LeaderboardEntry> getEntries() {
        return entries;
    }
}
