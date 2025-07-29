package plugin.borealcore.functions.jade.object;

import java.util.UUID;

public class LeaderboardEntry {
    private final UUID playerUUID;
    private final String playerName;
    private final int totalAmount;
    private final int position;

    public LeaderboardEntry(UUID playerUUID, String playerName, int totalAmount, int position) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.totalAmount = totalAmount;
        this.position = position;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
    public String getPlayerName() {
        return playerName;
    }
    public double getTotalAmount() {
        return totalAmount;
    }
    public int getPosition() {
        return position;
    }
}
