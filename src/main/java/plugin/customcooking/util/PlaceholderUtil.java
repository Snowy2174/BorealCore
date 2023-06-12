package plugin.customcooking.util;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.configs.MasteryManager;

public class PlaceholderUtil extends PlaceholderExpansion {

    private final CustomCooking plugin;

    public PlaceholderUtil(CustomCooking plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ccooking";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SnowyOwl217";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
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

        if (parts[0].equalsIgnoreCase("ingcheck")) {
            String ingredient = parts[1];
            boolean playerHasIngredient = InventoryUtil.playerHasIngredient(player.getInventory(), ingredient);
            return playerHasIngredient ? "&a" : "&c"; // returns green if player has the item, red otherwise
        } else {
            String recipe = parts[1];
            int masteryCount = MasteryManager.getMasteryCount(player, recipe);
            int requiredMastery = MasteryManager.getRequiredMastery(recipe);

            if (parts[0].equalsIgnoreCase("masterycount")) {
                return String.valueOf(masteryCount);
            } else if (parts[0].equalsIgnoreCase("requiredmastery")) {
                return String.valueOf(requiredMastery);
            } else if (parts[0].equalsIgnoreCase("masteryprogress")) {
                return String.valueOf(((float) masteryCount / requiredMastery) * 100);
            }
        }
        return null;
    }

    public static String setPlaceholders(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static String setPlaceholders(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
