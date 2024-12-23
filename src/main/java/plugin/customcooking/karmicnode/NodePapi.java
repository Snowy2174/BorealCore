package plugin.customcooking.karmicnode;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.customcooking.manager.JadeManager;

public class NodePapi extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "node";
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
            return "";
        }

        String[] parts = identifier.split("_");
        if (parts.length < 1) {
            return "Placeholder Too Short";
        }

        switch (parts[0].toLowerCase()) {
            case "maxwave":
                return String.valueOf(NodeManager.getMaxWave(player.getName()));
            case "leaderboard":
                String index = parts[1];
                return NodeManager.getLeaderboardEntry(Integer.parseInt(index));
            default:
                // Invalid or unrecognized placeholder identifier
                return "Invalid Placeholder";
        }
    }

}
