package plugin.customcooking.jade;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.customcooking.manager.JadeManager;

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
            return "";
        }

        String[] parts = identifier.split("_");
        if (parts.length < 2) {
            return "";
        }

        switch (parts[0].toLowerCase()) {
            case "lim":
                String source = parts[1];
                return JadeManager.checkJadeLimit(player, source);
            case "source":

            case "total":
                return String.valueOf(JadeManager.getTotalJadeForPlayer(player));
            default:
                // Invalid or unrecognized placeholder identifier
                return "Invalid Placeholder";
        }
    }

}
