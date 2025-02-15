package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.functions.jade.Database;
import plugin.customcooking.functions.jade.JadeManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.utility.AdventureUtil;

import java.util.HashMap;
import java.util.Map;


public class JadeCommand implements CommandExecutor {

    private final JadeManager jadeManager;
    private final Database database;

    public JadeCommand() {
        this.jadeManager = CustomCooking.getJadeManager();
        this.database = CustomCooking.getDatabase();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        if (args.length == 0) {
            return false;
        }

        String subcommand = args[0];
        String[] subargs = new String[args.length - 1];
        System.arraycopy(args, 1, subargs, 0, subargs.length);


        // Player Commands

        if (subcommand.equalsIgnoreCase("limits")) {
            handleLimitsCommand(sender);
        } else if (subcommand.equalsIgnoreCase("balance")) {
            handleBalanceCommand(sender);
        } else if (subcommand.equalsIgnoreCase("top")) {
            handleJadeLeaderboardCommand(sender);
        }

        if (!sender.hasPermission("customcooking.admin")) {
            return true;
        }

        // Admin Commands

        if (subcommand.equalsIgnoreCase("give")) {
            handleGiveJadeCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("remove")) {
            handleRemoveJadeCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("totalJadeForPlayer")) {
            handleTotalJadeForPlayerCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("totalJadeForSource")) {
            handleTotalJadeForSourceCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("getMostRecent")) {
            handleGetMostRecent(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("getPlayerData")) {
            handleGetPlayerData(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("verifyAndFixTotals")) {
            handleVerifyAndFixTotals(sender);
        } else {
            return false;
        }
        return true;
    }

    private void handleJadeLeaderboardCommand(CommandSender sender) {
        HashMap<String, Integer> leaderboard = jadeManager.getJadeLeaderboard();
        for (Map.Entry<String, Integer> entry : leaderboard.entrySet()) {
            AdventureUtil.sendMessage(sender, "Player: " + entry.getKey() + " | Jade: " + entry.getValue());
        }
    }

    private void handleLimitsCommand(CommandSender sender) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        for (String source : new String[]{"cooking", "farming", "fishing", "spirit", "mastery"}) {
            int limit = JadeManager.getLimitForSource(source);
            int total = database.getRecentPositiveTransactionTimestamps(player, source).size();
            AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Limit for " + source + ": " + total + "/" + limit);
        }

    }

    private void handleBalanceCommand(CommandSender sender) {

        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        int totalJade = database.getJadeForPlayer(player);
        AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Total jade: " + totalJade);
    }

    private void handleRemoveJadeCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade remove <player> <amount> <source>");
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
            amount = Integer.parseInt(args[1]);
            source = args[2];
        } else {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade remove <player> <amount> <source>");
            return;
        }
        JadeManager.remove(player, amount, source);
        AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Removed" + amount + " from " + source + " from " + player.getName());
    }

    private void handleTotalJadeForPlayerCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade totalJadeForPlayer <player>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }

        int totalJade = database.getJadeForPlayer(player);
        AdventureUtil.sendMessage(sender, "Total jade for " + player.getName() + ": " + totalJade);
    }

    private void handleTotalJadeForSourceCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade totalJadeForSource <source>");
            return;
        }

        String source = args[0];
        int totalJade = database.getTotalJadeFromSource(source);
        AdventureUtil.sendMessage(sender, "Total jade for source " + source + ": " + totalJade);
    }

    private void handleGetMostRecent(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade getMostRecent <player>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }

        String source = args.length > 1 ? args[1] : "";
        AdventureUtil.sendMessage(sender, "Most recent transaction for " + player.getName() + ": " + database.getRecentPositiveTransactionTimestamps(player, source));
    }

    private void handleGiveJadeCommand(CommandSender sender, String[] args) {
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
        JadeManager.giveJadeCommand(player, source, amount);
        AdventureUtil.sendMessage(sender, "Gave " + amount + " from " + source + " to " + player.getName());
    }

    private void handleGetPlayerData(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade getPlayerData <player>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }

        int totalJade = database.getJadeForPlayer(player);
        AdventureUtil.sendMessage(sender, "Total jade for " + player.getName() + ": " + totalJade);
    }

    private void handleVerifyAndFixTotals(CommandSender sender) {
        database.verifyAndFixTotals();
        AdventureUtil.sendMessage(sender, "Jade totals verified and fixed");
    }

}
