package plugin.customcooking.functions.jade;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.configs.ConfigManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;
import plugin.customcooking.utility.GUIUtil;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.bukkit.Bukkit.getServer;

public class JadeManager extends Function {

    protected static JadeDatabase database;

    public static void giveJadeCommand(Player player, String source, Integer amount) {
        if (database.getRecentPositiveTransactionTimestamps(player, source).size() <= getLimitForSource(source)) {
            give(player, amount, source);
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + "You've reached your limit for Jade from " + source + " today, try again later.");
        }
    }

    public static void give(Player player, double amount, String source) {
        boolean first = source.isBlank() || database.getRecentPositiveTransactionTimestamps(player, source).isEmpty();

        if (first && !source.isEmpty()) {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + "This is the first time you've gotten Jade from " + GUIUtil.formatString(source) + " today, you have " + getLimitForSource(source) + " remaining.");
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + "You have received " + amount + " Jade");
        }

        String command = "av User " + player.getName() + " AddPoints " + (int) amount;
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);

        database.addTransaction(player, amount, source, LocalDateTime.now());

        String bcast = MessageManager.infoPositive + "Whilst " + (source.isEmpty() ? "playing" : GUIUtil.formatString(source)) + ", " + player.getName() + " has found " + (int) amount + "₪";
        getServer().broadcast(AdventureUtil.getComponentFromMiniMessage(bcast));
    }

    public static void remove(Player player, double amount, String source) {
        String command = "av User " + player.getName() + " RemovePoints " + (int) amount;
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);

        database.addTransaction(player, -amount, source, LocalDateTime.now());
    }

    public static int getLimitForSource(String source) {
        if (source.isEmpty()) {
            return Integer.MAX_VALUE; // No limit if no source is specified
        }
        return switch (source) {
            case "fishing" -> ConfigManager.fishingLimit;
            case "cooking" -> ConfigManager.cookingLimit;
            case "farming" -> ConfigManager.cropsLimit;
            case "spirits" -> ConfigManager.spiritLimit;
            default -> 100;
        };
    }

    public static String checkJadeLimit(Player player, String source) {
        return database.getRecentPositiveTransactionTimestamps(player, source).size() + "/" + getLimitForSource(source);
    }

    public static int getTotalJadeForPlayer(Player player) {
        return database.getJadeForPlayer(player);
    }

    // @TODO Implement migrate Legacy jade data

    @Override
    public void load() {
        AdventureUtil.consoleMessage("[CustomCooking] Initialised Jade limit system");
        database = CustomCooking.getDatabase();
        database.verifyAndFixTotals();
    }

    @Override
    public void unload() {
    }

    public HashMap<String, Integer> getJadeLeaderboard() {
        return database.getJadeLeaderboard();
    }
}