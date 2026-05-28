package plugin.borealcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.CookingManager;
import plugin.borealcore.manager.GuiManager;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.utility.AdventureUtil;

public class RecipeBookCommand implements CommandExecutor {

    private final CookingManager cookingManager;

    public RecipeBookCommand() {
        this.cookingManager = BorealCore.getCookingManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("borealcore.admin")) {
            // @TODO: Add command information message
            return true;
        }

        if (args.length == 0) {
            // @TODO: Add command information message
            return false;
        }

        String subcommand = args[0];
        String[] subargs = new String[args.length - 1];
        System.arraycopy(args, 1, subargs, 0, subargs.length);

        if (subcommand.equalsIgnoreCase("cooking")) {
            handleCookingCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("brewing")) {
            handleBrewingCommand(sender, subargs);
        } else {
            // Unknown subcommand
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.unavailableArgs);
        }
        return true;
    }

    private void handleCookingCommand(CommandSender sender, String[] subargs) {
        if (sender instanceof Player player) {
            GuiManager.getCookingRecipeBook(null).open(player);
        }

        Player player = Bukkit.getPlayer(subargs[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        GuiManager.getCookingRecipeBook(null).open(player);
    }

    private void handleBrewingCommand(CommandSender sender, String[] subargs) {
        if (sender instanceof Player player) {
            GuiManager.getBrewingRecipeBook().open(player);
        }

        Player player = Bukkit.getPlayer(subargs[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        GuiManager.getBrewingRecipeBook().open(player);
    }
}



