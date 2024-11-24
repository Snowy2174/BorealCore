package plugin.customcooking.manager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import plugin.customcooking.CustomCooking;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import plugin.customcooking.jade.Error;
import plugin.customcooking.jade.Errors;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.util.AdventureUtil;

import static org.bukkit.Bukkit.getServer;
import static plugin.customcooking.manager.configs.ConfigManager.*;
import static plugin.customcooking.manager.configs.ConfigManager.spiritLimit;
import static plugin.customcooking.util.GUIUtil.formatString;

public class JadeManager extends Function {
    static CustomCooking plugin;
    Connection connection;
    // The name of the table we created back in SQLite class.
    public String table = "jade_transactions";
    public int tokens = 0;
    public static Map<Player, Map<String, Integer>> LIMITS;

    @Override
    public void load() {
        LIMITS = new HashMap<>();
        AdventureUtil.consoleMessage("[CustomCooking] Initialised Jade limit system");
    }

    @Override
    public void unload() {
        if (LIMITS != null) LIMITS.clear();
    }

    public static void handleGiveJadeCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade give <player> <source> <amount>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }

        String source = args[1];
        Integer amount = Integer.parseInt(args[2]);

        if (!LIMITS.containsKey(player)) {
            HashMap<String, Integer> sourceMap = new HashMap<>();
            sourceMap.put(source, amount);
            LIMITS.put(player, sourceMap);
            giveJade(player, amount, source, true);
            return;
        }

        Map<String, Integer> jadeMap = LIMITS.get(player);

        if (LIMITS.get(player).containsKey(source)) {
            Integer newJade = jadeMap.get(source) + amount;
            if (newJade > getLimitForSource(source)) {
                AdventureUtil.sendMessage(player, MessageManager.infoNegative + "You've reached your limit for Jade from " + source + " today, try again later.");
                return;
            }
            jadeMap.replace(source, newJade);
            giveJade(player, amount, source, false);

        } else {
            LIMITS.get(player).put(source, amount);
            giveJade(player, amount, source, true);
        }

        AdventureUtil.sendMessage(sender, "Gave " + amount + " from " + source + " to " + player.getName());
    }

    private static void giveJade(Player player, Integer amount, String source, boolean First) {
        String command = "av User " + player.getName() + " AddPoints " + amount.toString();
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);

        if (First) {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + "This is the first time you've gotten Jade from " + formatString(source) + " today, you have " + getLimitForSource(source) + " remaining.");
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + "You have received " + amount + " Jade");
        }

        CustomCooking.getDatabase().addTransaction(player, amount, source, LocalDateTime.now());
        String bcast = MessageManager.infoPositive + "Whilst " + formatString(source) + ", " + player.getName() + " has found " + amount.toString() + "₪";
        getServer().broadcast(AdventureUtil.getComponentFromMiniMessage(bcast));

    }

    // Integer totalAmount = getAggregatedData("playerName", "amount", null, null);
    // Integer sourceAmount = getAggregatedData("playerName", "amount", "source", "sourceName");
    // int totalJadeForPlayer = getTotalFromTransactions("player", player);
    // int totalJadeForSource = getTotalFromTransactions("source", source);
    // @TODO Implement remove jade method
    // @TODO Implement tab Autocomplete
    // @TODO Implement migrate Legacy jade data

    private static int getLimitForSource(String source) {
        switch (source) {
            case "fishing":
                return fishingLimit;
            case "cooking":
                return cookingLimit;
            case "farming":
                return cropsLimit;
            case "spirit":
                return spiritLimit;
            default:
                throw new IllegalArgumentException("Unknown source: " + source);
        }
    }

    public static String checkJadeLimit(Player player, String source) {
        if (LIMITS.containsKey(player) && (LIMITS.get(player).containsKey(source))) {
            return LIMITS.get(player).get(source) + "/" + getLimitForSource(source);
        }
        return "0/" + getLimitForSource(source);
    }

    public static int getTotalJadeForPlayer(Player player) {
        return CustomCooking.getDatabase().getTotalJadeForPlayer(player);
    }
}