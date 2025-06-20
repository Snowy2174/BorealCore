package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.BorealCore;
import plugin.customcooking.functions.wiki.WikiManager;
import plugin.customcooking.manager.GuiManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.utility.AdventureUtil;

import java.io.File;
import java.io.IOException;


public class WikiCommand implements CommandExecutor {

    private final WikiManager wikiManager;

    public WikiCommand() {
        this.wikiManager = BorealCore.getWikiManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            GuiManager.getWikiMenu().open((Player) sender);
            return true;
        }

        if (!sender.hasPermission("customcooking.admin")) {
            return true;
        }

        String subcommand = args[0];
        String[] subargs = new String[args.length - 1];
        System.arraycopy(args, 1, subargs, 0, subargs.length);

        if (subcommand.equalsIgnoreCase("open")) {
            handleOpenBookCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("updateWiki")) {
            try {
                WikiManager.downloadRepo("https://github.com/Snowy2174/BendingMC-Wiki.git", new File(BorealCore.plugin.getDataFolder() + File.separator + "wiki"), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
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
        WikiManager.openBook(player, id);
        AdventureUtil.sendMessage(sender, "Opened wiki page " + id + " for " + player.getName());
    }
}
