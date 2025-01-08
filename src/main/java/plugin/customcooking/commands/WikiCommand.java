package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.karmicnode.NodeManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.InventoryUtil;
import plugin.customcooking.wiki.WikiManager;


public class WikiCommand implements CommandExecutor {

    private final WikiManager wikiManager;

    public WikiCommand() {
        this.wikiManager = CustomCooking.getWikiManager();
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

        if (subcommand.equalsIgnoreCase("open")) {
            handleOpenBookCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("update")) {

        } else if (subcommand.equalsIgnoreCase("getLeaderboardEntry")) {
        }
        else {
            return false;

        }
        return true;
    }

    private void handleOpenBookCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/wiki open <page> <player>");
            return;
        }
        String id = args[0];
        Player player = (args.length < 2) ? (Player) sender : Bukkit.getPlayer(args[1]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        wikiManager.openBook(player, id);
        AdventureUtil.sendMessage(sender, "Opened wiki page " + id + " for " + player.getName());
    }
}
