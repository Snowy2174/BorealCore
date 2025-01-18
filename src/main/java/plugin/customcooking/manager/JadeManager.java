package plugin.customcooking.manager;

import org.bukkit.Bukkit;
import plugin.customcooking.CustomCooking;

import java.time.LocalDateTime;

import org.bukkit.entity.Player;
import plugin.customcooking.functions.jade.JadeDatabase;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.utility.AdventureUtil;

import static org.bukkit.Bukkit.getServer;
import static plugin.customcooking.manager.configs.ConfigManager.*;
import static plugin.customcooking.manager.configs.ConfigManager.spiritLimit;
import static plugin.customcooking.utility.GUIUtil.formatString;

public class JadeManager extends Function {

    protected static JadeDatabase database;

    @Override
    public void load() {
        AdventureUtil.consoleMessage("[CustomCooking] Initialised Jade limit system");
        this.database = CustomCooking.getDatabase();
        database.verifyAndFixTotals();
    }

    @Override
    public void unload() {
    }

    public static void giveJadeCommand(Player player, String source, Integer amount) {
        if (database.getRecentPositiveTransactionTimestamps(player, source).size() <= getLimitForSource(source)) {
            give(player, amount, source);
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoNegative + "You've reached your limit for Jade from " + source + " today, try again later.");
        }
    }
    static void give(Player player, double amount, String source) {
        boolean first = source.isBlank() ||  database.getRecentPositiveTransactionTimestamps(player, source).isEmpty();

        if (first && !source.isEmpty()) {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + "This is the first time you've gotten Jade from " + formatString(source) + " today, you have " + getLimitForSource(source) + " remaining.");
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + "You have received " + amount + " Jade");
        }

        String command = "av User " + player.getName() + " AddPoints " + (int)amount;
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);

        database.addTransaction(player, amount, source, LocalDateTime.now());

        String bcast = MessageManager.infoPositive + "Whilst " + (source.isEmpty() ? "playing" : formatString(source)) + ", " + player.getName() + " has found " + (int)amount + "₪";
        getServer().broadcast(AdventureUtil.getComponentFromMiniMessage(bcast));
    }

    public static void remove(Player player, double amount, String source) {
        String command = "av User " + player.getName() + " RemovePoints " + (int)amount;
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);

        database.addTransaction(player, -amount, source, LocalDateTime.now());
    }

    private static int getLimitForSource(String source) {
        if (source.isEmpty()) {
            return Integer.MAX_VALUE; // No limit if no source is specified
        }
        return switch (source) {
            case "fishing" -> fishingLimit;
            case "cooking" -> cookingLimit;
            case "farming" -> cropsLimit;
            case "spirit" -> spiritLimit;
            default -> 0;
        };
    }

    // @TODO Implement migrate Legacy jade data

    public static String checkJadeLimit(Player player, String source) {
        return database.getRecentPositiveTransactionTimestamps(player, source).size() + "/" + getLimitForSource(source);
    }

    public static int getTotalJadeForPlayer(Player player) {
        return database.getJadeForPlayer(player);
    }
}