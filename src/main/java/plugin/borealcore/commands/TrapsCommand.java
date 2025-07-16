package plugin.borealcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.borealcore.functions.traps.TrapDataManager;
import plugin.borealcore.manager.MasteryManager;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.utility.AdventureUtil;
import plugin.borealcore.utility.RecipeDataUtil;

import java.util.List;

public class TrapsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("borealcore.admin")) {
            showStatsCommand(sender);
            return true;
        }

        if (args.length == 0) {
            showCommandHelp(sender);
            return false;
        }

        String subcommand = args[0];
        String[] subargs = new String[args.length - 1];
        System.arraycopy(args, 1, subargs, 0, subargs.length);

        if (subcommand.equalsIgnoreCase("updateall")) {
            handleUpdateTrapsCommand(sender, subargs);
        } else {
            // Unknown subcommand
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.unavailableArgs);
        }

        return true;
    }

    private void showStatsCommand(CommandSender sender) { //@TODO UPDATE
        if (sender instanceof Player player) {
            List<String> unlockedRecipes = RecipeDataUtil.getUnlockedRecipes(player);

            AdventureUtil.sendMessage(sender, "<gold><bold>BorealCore</bold><grey> version 1.1.4");
            AdventureUtil.sendMessage(sender, "<grey>Created by <gold>SnowyOwl217");
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Cooked: " + MasteryManager.getRecipeCount(player.getName()));
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Unlocked: " + unlockedRecipes.size());
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Mastered: " + RecipeDataUtil.getMasteredRecipes(player, unlockedRecipes).size());
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Unknown: " + RecipeDataUtil.getLockedRecipes(unlockedRecipes).size());
        }
    }

    private void showCommandHelp(CommandSender sender) { //@TODO UPDATE
        AdventureUtil.sendMessage(sender, "<gold><bold>BorealCore</bold><grey> version 1.0.0");
        AdventureUtil.sendMessage(sender, "<grey>Created by <gold>SnowyOwl217");
        AdventureUtil.sendMessage(sender, "<gold>/cooking cook <recipe> <player> [auto]");
        AdventureUtil.sendMessage(sender, "<gold>/cooking unlock <player> <recipe>");
        AdventureUtil.sendMessage(sender, "<gold>/cooking lock <player> <recipe>");
        AdventureUtil.sendMessage(sender, "<gold>/cooking competition <start/end/cancel>");
        AdventureUtil.sendMessage(sender, "<gold>/cooking reload");
        AdventureUtil.sendMessage(sender, "<gold>/cooking migrateperms");
    }

    private void handleUpdateTrapsCommand(CommandSender sender, String[] args) {
        //if (args.length < 2) {
        //    AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/traps updateall");
        //    return;
        //}
        TrapDataManager.updateFishingTraps();
    }


}
