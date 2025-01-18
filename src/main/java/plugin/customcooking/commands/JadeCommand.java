package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.functions.jade.JadeDatabase;
import plugin.customcooking.manager.JadeManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.utility.AdventureUtil;


public class JadeCommand implements CommandExecutor {

    private final JadeManager jadeManager;
    private final JadeDatabase jadeDatabase;

    public JadeCommand() {
        this.jadeManager = CustomCooking.getJadeManager();
        this.jadeDatabase = CustomCooking.getDatabase();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customcooking.admin")) {
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        String subcommand = args[0];
        String[] subargs = new String[args.length - 1];
        System.arraycopy(args, 1, subargs, 0, subargs.length);

        if (subcommand.equalsIgnoreCase("give")) {
            handleGiveJadeCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("remove")) {
            handleRemoveJadeCommand(sender, subargs);
        }
        else if (subcommand.equalsIgnoreCase("totalJadeForPlayer")) {
            handleTotalJadeForPlayerCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("totalJadeForSource")) {
            handleTotalJadeForSourceCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("getMostRecent")) {
            handleGetMostRecent(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("getPlayerData")) {
            handleGetPlayerData(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("verifyAndFixTotals")) {
            handleVerifyAndFixTotals(sender);
        }
        else {
            return false;
        }
        return true;
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
        jadeManager.remove(player, amount, source);
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

        int totalJade = jadeDatabase.getJadeForPlayer(player);
        AdventureUtil.sendMessage(sender, "Total jade for " + player.getName() + ": " + totalJade);
    }

    private void handleTotalJadeForSourceCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade totalJadeForSource <source>");
            return;
        }

        String source = args[0];
        int totalJade = jadeDatabase.getTotalJadeFromSource(source);
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
        AdventureUtil.sendMessage(sender, "Most recent transaction for " + player.getName() + ": " + jadeDatabase.getRecentPositiveTransactionTimestamps(player, source));
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
        jadeManager.giveJadeCommand(player, source, amount);
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

        int totalJade = jadeDatabase.getJadeForPlayer(player);
        AdventureUtil.sendMessage(sender, "Total jade for " + player.getName() + ": " + totalJade);
    }

    private void handleVerifyAndFixTotals(CommandSender sender) {
        jadeDatabase.verifyAndFixTotals();
        AdventureUtil.sendMessage(sender, "Jade totals verified and fixed");
    }

}
