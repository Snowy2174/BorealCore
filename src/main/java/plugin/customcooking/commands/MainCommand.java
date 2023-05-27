package plugin.customcooking.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.util.InventoryUtil;

import java.util.Arrays;

import static org.bukkit.Bukkit.getPlayer;
import static plugin.customcooking.configs.RecipeManager.addRecipe;
import static plugin.customcooking.configs.RecipeManager.checkAndAddRandomRecipe;

public class MainCommand implements CommandExecutor {

    private final InventoryUtil inventoryUtil;

    public MainCommand() {
        this.inventoryUtil = new InventoryUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("customcooking.admin")) {
            AdventureUtil.sendMessage(sender, "<red>You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            AdventureUtil.sendMessage(sender, "<gold><bold>CustomCooking</bold><grey> version 1.0.0");
            AdventureUtil.sendMessage(sender, "<grey>Created by <gold>SnowyOwl217");
            AdventureUtil.sendMessage(sender, "<gold>/cook cook <recipe> <player> [auto]");
            AdventureUtil.sendMessage(sender, "<gold>/cook reload");
            return false;
        }
        String subcommand = args[0];
        String[] subargs = Arrays.copyOfRange(args, 1, args.length);

        if (subcommand.equalsIgnoreCase("cook")) {
            if (subargs.length < 2) {
                AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking cook <recipe> <player> <auto>");
                return true;
            }
            String recipe = subargs[0];
            String username = subargs[1];
            if (subargs.length == 2) {
                Cook(recipe, username, false);
            } else if (subargs.length == 3 && subargs[2].equalsIgnoreCase("auto")){
                Cook(recipe, username, true);
            } else {
                AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red>/cooking cook <recipe> <player> <auto>");
            } return true;
        } else if (subcommand.equalsIgnoreCase("reload")) {
            Reload(sender);
            return true;
        } else if (subcommand.equalsIgnoreCase("unlock")) {
            if (subargs.length < 1) {
                AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking unlock <player> <recipe>");
                return true;
            }
            String username = subargs[0];
            String recipe = subargs[1];
            if (subargs.length == 1) {
                checkAndAddRandomRecipe(getPlayer(username));
            }
            if (subargs.length == 2) {
                addRecipe(getPlayer(username), recipe);
            } else {
                AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red>/cooking cook <recipe> <player> <auto>");
            }
            return true;
        }
            else {
            // Unknown subcommand
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> Unknown subcommand: " + subcommand);
            return false;
        }
    }


    public void Cook(String recipe, String username, Boolean auto) {
        Player player = getPlayer(username);
        if (player != null) {
            inventoryUtil.ingredientCheck(player, recipe, auto);
        } else {
            AdventureUtil.consoleMessage("<Red> [!] Player " + username + " not found.");
        }
    }

    public void Reload(CommandSender sender) {
        long time1 = System.currentTimeMillis();
        ConfigUtil.reload();
        AdventureUtil.sendMessage(sender, "<gray>[CustomCooking] Reloaded plugin in <green>" + (System.currentTimeMillis() - time1) + " <gray>seconds");
    }
}
