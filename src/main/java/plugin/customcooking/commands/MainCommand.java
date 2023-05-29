package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.configs.RecipeManager;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.util.InventoryUtil;

public class MainCommand implements CommandExecutor {

    private final InventoryUtil inventoryUtil;

    public MainCommand() {
        this.inventoryUtil = new InventoryUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customcooking.admin")) {
            AdventureUtil.sendMessage(sender, "<red>You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            showCommandHelp(sender);
            return false;
        }

        String subcommand = args[0];
        String[] subargs = new String[args.length - 1];
        System.arraycopy(args, 1, subargs, 0, subargs.length);

        if (subcommand.equalsIgnoreCase("cook")) {
            handleCookCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("reload")) {
            handleReloadCommand(sender);
        } else if (subcommand.equalsIgnoreCase("unlock")) {
            handleUnlockCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("lock")) {
            handleLockCommand(sender, subargs);
        } else {
            // Unknown subcommand
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> Unknown subcommand: " + subcommand);
        }

        return true;
    }

    //TODO: Add command to edit Mastery
    //TODO: Add command to lock recipes

    private void showCommandHelp(CommandSender sender) {
        AdventureUtil.sendMessage(sender, "<gold><bold>CustomCooking</bold><grey> version 1.0.0");
        AdventureUtil.sendMessage(sender, "<grey>Created by <gold>SnowyOwl217");
        AdventureUtil.sendMessage(sender, "<gold>/cook cook <recipe> <player> [auto]");
        AdventureUtil.sendMessage(sender, "<gold>/cook unlock <player> <recipe>");
        AdventureUtil.sendMessage(sender, "<gold>/cook lock <player> <recipe>");
        AdventureUtil.sendMessage(sender, "<gold>/cook reload");
    }

    private void handleCookCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking cook <recipe> <player> <auto>");
            return;
        }

        String recipe = args[0];
        String username = args[1];
        boolean auto = args.length == 3 && args[2].equalsIgnoreCase("auto");
        cook(recipe, username, auto);
    }

    private void handleReloadCommand(CommandSender sender) {
        long startTime = System.currentTimeMillis();
        ConfigUtil.reload();
        AdventureUtil.sendMessage(sender, "<gray>[CustomCooking] Reloaded plugin in <green>" + (System.currentTimeMillis() - startTime) + " <gray>seconds");
    }

    private void handleUnlockCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking unlock <player> <recipe>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.consoleMessage("<Red> [!] Player " + args[0] + " not found.");
            return;
        }

        if (args.length == 1) {
            RecipeManager.checkAndAddRandomRecipe(player);
        } else if (args.length == 2) {
            String recipe = args[1];
            RecipeManager.addRecipe(player, recipe);
        } else {
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking unlock <player> <recipe>");
        }
    }

    private void handleLockCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking lock <player> <recipe>");
            return;
        }

        if (args.length == 2) {
            Player player = Bukkit.getPlayer(args[0]);
            String recipe = args[1];
            if (player == null) {
                AdventureUtil.consoleMessage("<Red> [!] Player " + args[0] + " not found.");
                return;
            }
            RecipeManager.removeRecipe(player, recipe);
        } else {
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking lock <player> <recipe>");
        }
    }

    private void cook(String recipe, String username, boolean auto) {
        Player player = Bukkit.getPlayer(username);
        if (player != null) {
            inventoryUtil.ingredientCheck(player, recipe, auto);
        } else {
            AdventureUtil.consoleMessage("<Red> [!] Player " + username + " not found.");
        }
    }
}
