package plugin.customcooking.cooking;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.customcooking.manager.DataManager;
import plugin.customcooking.util.InventoryUtil;
import plugin.customcooking.util.RecipeDataUtil;

public class CookingPapi extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "customcooking";
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

        String recipe;
        Integer masteryCount;
        Integer requiredMastery;
        switch (parts[0].toLowerCase()) {
            case "ingcheck":
                String ingredient = parts[1];
                boolean playerHasIngredient = InventoryUtil.playerHasIngredient(player.getInventory(), ingredient);
                return playerHasIngredient ? "&a" : "&c"; // returns green if player has the item, red otherwise
            case "cooking-stats":
                String playerName = parts[1];
                return String.valueOf(DataManager.getRecipeCount(playerName));
            case "masterycount":
                recipe = parts[1];
                masteryCount = RecipeDataUtil.getMasteryCount(player, recipe);
                return String.valueOf(masteryCount);
            case "requiredmastery":
                recipe = parts[1];
                requiredMastery = RecipeDataUtil.getDefaultRequiredMastery(recipe);
                return String.valueOf(requiredMastery);
            case "masteryprogress":
                recipe = parts[1];
                masteryCount = RecipeDataUtil.getMasteryCount(player, recipe);
                requiredMastery = RecipeDataUtil.getDefaultRequiredMastery(recipe);
                float masteryProgress = ((float) masteryCount / requiredMastery) * 100;
                return String.valueOf(masteryProgress);
            default:
                // Invalid or unrecognized placeholder identifier
                return "Invalid Placeholder";
        }
    }


}
