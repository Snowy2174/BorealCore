package plugin.borealcore.jade.object;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.borealcore.jade.JadeModule;
import plugin.borealcore.jade.config.JadeDatabase;
import plugin.borealcore.jade.config.JadeMessage;

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
    public String getRequiredPlugin() {
        return "BorealCore";
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
                return JadeModule.checkJadeLimit(player, source);
            case "leaderboard":
                if (parts.length < 3) {
                    return "Invalid Leaderboard Placeholder";
                }
                LeaderboardType leaderboardType = LeaderboardType.valueOf(parts[1].toUpperCase());
                int position = Integer.parseInt(parts[2]);
                if (leaderboardType != null && position >= 0) {
                    LeaderboardEntry leaderboard = JadeDatabase.leaderboardCache.get(leaderboardType).getEntry(position);
                    if (parts.length > 3 && parts[3].equalsIgnoreCase("alt")) {
                        return JadeMessage.altLeaderboardEntry
                                .replace("{player}", leaderboard.getPlayerName())
                                .replace("{position}", String.valueOf(position))
                                .replace("{score}", String.valueOf(leaderboard.getTotalAmount()));
                    }

                    return JadeMessage.leaderboardEntry
                            .replace("{player}", leaderboard.getPlayerName())
                            .replace("{position}", String.valueOf(position))
                            .replace("{score}", String.valueOf(leaderboard.getTotalAmount()));
                } else {
                    return "Invalid Leaderboard Type";
                }
            case "total":
                return String.valueOf(JadeModule.getTotalJadeForPlayer(player));
            default:
                // Invalid or unrecognized placeholder identifier
                return "Invalid Placeholder";
        }
    }

}
