package plugin.customcooking.manager;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import plugin.customcooking.CustomCooking;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import plugin.customcooking.jade.JadeTransaction;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.object.Function;
import plugin.customcooking.util.AdventureUtil;

import static org.bukkit.Bukkit.getServer;
import static plugin.customcooking.manager.configs.ConfigManager.*;
import static plugin.customcooking.manager.configs.ConfigManager.spiritLimit;
import static plugin.customcooking.util.GUIUtil.formatString;

public class JadeManager extends Function {
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
        if (args.length < 2) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade give <player> <source> <amount>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        String source = "";
        int amount = 0;
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }

        if (args.length == 2 && args[1].matches("-?\\d+(\\.\\d+)?")) {
            amount = Integer.parseInt(args[1]);
        } else if (args.length == 3 && args[2].matches("-?\\d+(\\.\\d+)?")) {
            source = args[1];
            amount = Integer.parseInt(args[2]);
        } else {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade give <player> <source> <amount>");
            return;
        }

        JadeTransaction jadeTransaction = new JadeTransaction(player.getName(), amount, source, LocalDateTime.now());

        if (isMoreThan24HoursLater(CustomCooking.getDatabase().getMostRecentPositiveTransactionTimestamp(player, source))) {
            // Handle 24 hours check
        }

        if (!source.isEmpty()) {
            if (!LIMITS.containsKey(player)) {
                HashMap<String, Integer> sourceMap = new HashMap<>();
                sourceMap.put(source, amount);
                LIMITS.put(player, sourceMap);
                giveJade(player, amount, source);
                return;
            }

            Map<String, Integer> jadeMap = LIMITS.get(player);

            if (jadeMap.containsKey(source)) {
                Integer newJade = jadeMap.get(source) + amount;
                if (newJade > getLimitForSource(source)) {
                    AdventureUtil.sendMessage(player, MessageManager.infoNegative + "You've reached your limit for Jade from " + source + " today, try again later.");
                    return;
                }
                jadeMap.replace(source, newJade);
            } else {
                jadeMap.put(source, amount);
            }

            AdventureUtil.sendMessage(sender, "Gave " + amount + " from " + source + " to " + player.getName());
        } else {
            AdventureUtil.sendMessage(sender, "Gave " + amount + " jade to " + player.getName());
        }
        giveJade(player, amount, source);
    }
    static void giveJade(Player player, double amount, String source) {
        boolean first = source.isEmpty() || !LIMITS.get(player).containsKey(source) || LIMITS.get(player).get(source) == amount;

        if (first && !source.isEmpty()) {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + "This is the first time you've gotten Jade from " + formatString(source) + " today, you have " + getLimitForSource(source) + " remaining.");
        } else {
            AdventureUtil.sendMessage(player, MessageManager.infoPositive + "You have received " + amount + " Jade");
        }

        String command = "av User " + player.getName() + " AddPoints " + (int)amount;
        Bukkit.dispatchCommand(getServer().getConsoleSender(), command);

        CustomCooking.getDatabase().addTransaction(player, amount, source, LocalDateTime.now());
        String bcast = MessageManager.infoPositive + "Whilst " + (source.isEmpty() ? "playing" : formatString(source)) + ", " + player.getName() + " has found " + amount + "₪";
        getServer().broadcast(AdventureUtil.getComponentFromMiniMessage(bcast));
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

    // Integer totalAmount = getAggregatedData("playerName", "amount", null, null);
    // Integer sourceAmount = getAggregatedData("playerName", "amount", "source", "sourceName");
    // int totalJadeForPlayer = getTotalFromTransactions("player", player);
    // int totalJadeForSource = getTotalFromTransactions("source", source);
    // @TODO Implement remove jade method
    // @TODO Implement tab Autocomplete
    // @TODO Implement migrate Legacy jade data

    public static boolean isMoreThan24HoursLater(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(dateTime.plusHours(24));
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