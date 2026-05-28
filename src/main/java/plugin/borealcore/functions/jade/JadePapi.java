package plugin.borealcore.functions.jade;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.borealcore.functions.jade.object.LeaderboardEntry;
import plugin.borealcore.manager.configs.MessageManager;

public class JadePapi extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "jade";
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
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "No Player";
        }

        String[] parts = identifier.split("_");
        if (parts.length < 1) {
            return "Placeholder Too Short";
        }

        switch (parts[0].toLowerCase()) {
            case "lim":
                String source = parts[1];
                return JadeManager.checkJadeLimit(player, source);
            case "leaderboard":
                if (parts.length < 3) {
                    return "Invalid Leaderboard Placeholder";
                }
                LeaderboardType leaderboardType = LeaderboardType.valueOf(parts[1].toUpperCase());
                int position = Integer.parseInt(parts[2]);
                if (leaderboardType != null && position >= 0) {
                    LeaderboardEntry leaderboard = JadeManager.leaderboardCache.get(leaderboardType).getEntry(position);
                    if (parts.length > 3 && parts[3].equalsIgnoreCase("alt")) {
                        return MessageManager.altLeaderboardEntry
                                .replace("{player}", leaderboard.getPlayerName())
                                .replace("{position}", String.valueOf(position))
                                .replace("{score}", String.valueOf(leaderboard.getTotalAmount()));
                    }

                    return MessageManager.leaderboardEntry
                            .replace("{player}", leaderboard.getPlayerName())
                            .replace("{position}", String.valueOf(position))
                            .replace("{score}", String.valueOf(leaderboard.getTotalAmount()));
                } else {
                    return "Invalid Leaderboard Type";
                }
            case "total":
                return String.valueOf(JadeManager.getTotalJadeForPlayer(player));
            default:
                // Invalid or unrecognized placeholder identifier
                return "Invalid Placeholder";
        }
    }

}
