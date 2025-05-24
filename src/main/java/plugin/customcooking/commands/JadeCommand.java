package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.database.Database;
import plugin.customcooking.functions.jade.JadeManager;
import plugin.customcooking.functions.jade.Leaderboard;
import plugin.customcooking.functions.jade.LeaderboardEntry;
import plugin.customcooking.functions.jade.LeaderboardType;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.utility.AdventureUtil;

import static plugin.customcooking.functions.jade.JadeManager.reconsileJadeData;


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
        } else if (subcommand.equalsIgnoreCase("top") || subcommand.equalsIgnoreCase("leaderboard")) {
            handleLeaderboardCommand(sender, args);
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
        } else if (subcommand.equalsIgnoreCase("reconsile")) {
            handleReconsileCommand(sender, subargs);
        } else {
            return false;
        }
        return true;
    }

    private void handleLimitsCommand(CommandSender sender) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        JadeManager.sendJadeLimitMessage(player);
    }

    private void handleBalanceCommand(CommandSender sender) {

        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        database.getJadeForPlayerAsync(player, jade -> {
            AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Total jade: " + jade);
        });
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

        database.getJadeForPlayerAsync(player, jade -> {
            AdventureUtil.sendMessage(sender, "Total jade for " + player.getName() + ": " + jade);});
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

        database.getJadeForPlayerAsync(player, jade -> {
            AdventureUtil.sendMessage(sender, "Total jade for " + player.getName() + ": " + jade);});
    }

    private void handleVerifyAndFixTotals(CommandSender sender) {
        database.verifyAndFixTotals();
        AdventureUtil.sendMessage(sender, "Jade totals verified and fixed");
    }

    private void handleReconsileCommand(CommandSender sender, String[] args) {
        if (args.length < 0) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade reconsile>");
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
                reconsileJadeData(p);
                AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Reconciled jade data for " + p.getName());
                return;
        }
    }

private void handleLeaderboardCommand(CommandSender sender, String[] args) {
    if (args.length < 1) {
        AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade leaderboard <type> <page>");
        return;
    }
    LeaderboardType type = LeaderboardType.CURRENT;
    int page = 1;
    if (args.length > 2) {
        try {
            type = LeaderboardType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "Invalid leaderboard type");
            return;
        }
        try {
            page = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "Invalid page number");
            return;
        }
    }

    Leaderboard leaderboard = jadeManager.getLeaderboard(type);
    int entriesPerPage = 5;
    int totalEntries = leaderboard.getEntries().size();
    int totalPages = (int) Math.ceil((double) totalEntries / entriesPerPage);

    if (page < 1 || page > totalPages) {
        AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "Page out of range. Total pages: " + totalPages);
        return;
    }

    AdventureUtil.sendMessage(sender, MessageManager.leaderboardHeader
    .replace("{type}", type.toString())
            .replace("{page}", String.valueOf(page))
            .replace("{totalPages}", String.valueOf(totalPages)));
    leaderboard.getEntries().stream()
            .skip((long) (page - 1) * entriesPerPage)
            .limit(entriesPerPage)
            .forEach(entry -> AdventureUtil.sendMessage(sender, MessageManager.leaderboardEntry
                    .replace("{player}", entry.getPlayerName())
                    .replace("{position}", String.valueOf(entry.getPosition()))
                    .replace("{score}", String.valueOf(entry.getTotalAmount()))));
    AdventureUtil.sendMessage(sender, MessageManager.leaderboardFooter);
    AdventureUtil.sendMessage(sender, MessageManager.infoPositive + "Your position: " + leaderboard.getEntries()
            .stream()
            .filter(entry -> entry.getPlayerName().equalsIgnoreCase(sender.getName()))
            .findFirst()
            .map(LeaderboardEntry::getPosition)
            .orElse(0));
}

}
