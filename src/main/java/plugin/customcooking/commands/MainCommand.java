package plugin.customcooking.commands;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.configs.MasteryManager;
import plugin.customcooking.configs.RecipeManager;
import plugin.customcooking.gui.InventoryPopulator;
import plugin.customcooking.manager.CookingManager;
import plugin.customcooking.minigame.Product;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.util.InventoryUtil;

import java.util.List;

import static net.kyori.adventure.key.Key.key;
import static plugin.customcooking.configs.RecipeManager.successItems;
import static plugin.customcooking.util.AdventureUtil.playerSound;
import static plugin.customcooking.util.RecipeDataUtil.setRecipeData;

public class MainCommand implements CommandExecutor {

    private final InventoryUtil inventoryUtil;
    private final CookingManager cookingManager;

    public MainCommand() {
        this.inventoryUtil = new InventoryUtil();
        this.cookingManager = new CookingManager();
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
        } else if (subcommand.equalsIgnoreCase("mastery")) {
            handleMasteryCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("recipebook")) {
            handleRecipeBookCommand(sender, subargs);
        }
        else {
            // Unknown subcommand
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> Unknown subcommand: " + subcommand);
        }

        return true;
    }

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

        Player player = Bukkit.getPlayer(username);
        if (player != null) {
            cookingManager.handleCooking(recipe, player, auto);
        } else {
            AdventureUtil.consoleMessage("<Red> [!] Player " + username + " not found.");
        }

    }

    private void handleReloadCommand(CommandSender sender) {
        long startTime = System.currentTimeMillis();
        ConfigUtil.reload();
        AdventureUtil.sendMessage(sender, "<gray>[CustomCooking] Reloaded plugin in <green>" + (System.currentTimeMillis() - startTime) + "ms");
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
            if (args[1].equalsIgnoreCase("all")) {
                RecipeManager.unlockAllRecipes(player);
            } else {
                String recipe = args[1];
                RecipeManager.unlockRecipe(player, recipe);
            }
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
            RecipeManager.lockRecipe(player, recipe);
        } else {
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking lock <player> <recipe>");
        }
    }

    private void handleMasteryCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking mastery <player> <recipe> <count>");
            return;
        }

        if (args.length == 3) {
            Player player = Bukkit.getPlayer(args[0]);
            String recipe = args[1];
            Integer count = Integer.parseInt(args[2]);
            if (player == null) {
                AdventureUtil.consoleMessage("<Red> [!] Player " + args[0] + " not found.");
                return;
            }
            setRecipeData(player, recipe, count);
        } else {
            AdventureUtil.sendMessage(sender, "<grey>[<red><bold>!</bold><grey>]<red> /cooking mastery <player> <recipe> <count>\"");
        }
    }

    private void handleRecipeBookCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            InventoryPopulator.RECIPEBOOK.open(player);
        }
    }
}
