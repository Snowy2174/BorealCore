package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.cooking.competition.Competition;
import plugin.customcooking.cooking.competition.CompetitionSchedule;
import plugin.customcooking.gui.GuiManager;
import plugin.customcooking.manager.CookingManager;
import plugin.customcooking.manager.DataManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.util.InventoryUtil;
import plugin.customcooking.util.RecipeDataUtil;

import java.util.List;

import static plugin.customcooking.manager.DataManager.getRecipeCount;


public class MainCommand implements CommandExecutor {

    private final CookingManager cookingManager;

    public MainCommand() {
        this.cookingManager = CustomCooking.getCookingManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customcooking.admin")) {
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

        if (subcommand.equalsIgnoreCase("cook")) {
            handleCookCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("reload")) {
            handleReloadCommand(sender);
        } else if (subcommand.equalsIgnoreCase("migrateperms")) {
            handleMigratePermsCommand(sender);
        } else if (subcommand.equalsIgnoreCase("unlock")) {
            handleUnlockCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("lock")) {
            handleLockCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("mastery")) {
            handleMasteryCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("recipebook")) {
            handleRecipeBookCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("competition")){
            handleCompetitionCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("give")){
            handleGiveItemCommand(sender, subargs);
        }
        else {
            // Unknown subcommand
            AdventureUtil.sendMessage(sender,  MessageManager.infoNegative + MessageManager.unavailableArgs);
        }

        return true;
    }

    private void showStatsCommand(CommandSender sender) {
        if (sender instanceof Player player) {
            List<String> unlockedRecipes = RecipeDataUtil.getUnlockedRecipes(player);

            AdventureUtil.sendMessage(sender, "<gold><bold>CustomCooking</bold><grey> version 1.1.4");
            AdventureUtil.sendMessage(sender, "<grey>Created by <gold>SnowyOwl217");
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Cooked: " + getRecipeCount(player.getName()));
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Unlocked: " + unlockedRecipes.size());
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Mastered: " + RecipeDataUtil.getMasteredRecipes(player, unlockedRecipes).size());
            AdventureUtil.sendMessage(sender, "<gold> Total Recipes Unknown: " + RecipeDataUtil.getLockedRecipes(unlockedRecipes).size());
        }
    }

    private void showCommandHelp(CommandSender sender) {
        AdventureUtil.sendMessage(sender, "<gold><bold>CustomCooking</bold><grey> version 1.0.0");
        AdventureUtil.sendMessage(sender, "<grey>Created by <gold>SnowyOwl217");
        AdventureUtil.sendMessage(sender, "<gold>/cooking cook <recipe> <player> [auto]");
        AdventureUtil.sendMessage(sender, "<gold>/cooking unlock <player> <recipe>");
        AdventureUtil.sendMessage(sender, "<gold>/cooking lock <player> <recipe>");
        AdventureUtil.sendMessage(sender, "<gold>/cooking competition <start/end/cancel>");
        AdventureUtil.sendMessage(sender, "<gold>/cooking reload");
        AdventureUtil.sendMessage(sender, "<gold>/cooking migrateperms");
    }

    private void handleCookCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/cooking cook <recipe> <player> <auto>");
            return;
        }

        String recipe = args[0];
        String username = args[1];
        boolean auto = args.length == 4 && args[3].equalsIgnoreCase("auto");

        Player player = Bukkit.getPlayer(username);
        if (player != null) {
            if (auto) {
                cookingManager.handleAutocooking(recipe, player, Integer.valueOf(args[2]));
            } else {
                cookingManager.handleCooking(recipe, player, null);
            }
        } else {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
        }

    }

    private void handleReloadCommand(CommandSender sender) {
        long startTime = System.currentTimeMillis();
        ConfigUtil.reload();
        AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.reload.replace("{time}", String.valueOf(System.currentTimeMillis() - startTime)));
    }

    private void handleMigratePermsCommand(CommandSender sender) {
        long startTime = System.currentTimeMillis();
        int migratedCount = DataManager.migratePermissions();
        AdventureUtil.sendMessage(sender, MessageManager.prefix + " Migrated and Updated the perms for <green>" + migratedCount + "Recipes and Masteries <gray> in <green>" + (System.currentTimeMillis() - startTime) + "ms" );
    }

    private void handleUnlockCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/cooking unlock <player> <recipe>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.consoleMessage(MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }

        if (args.length == 1) {
            RecipeDataUtil.checkAndAddRandomRecipe(player);
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("all")) {
                RecipeDataUtil.unlockAllRecipes(player);
            } else if (args[1].equalsIgnoreCase("player")) {
                RecipeDataUtil.unlockStarterRecipes(player);
            } else {
                String recipe = args[1];
                RecipeDataUtil.setRecipeStatus(player, recipe, true);
            }
        } else {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/cooking unlock <player> <recipe>");
        }
    }

    private void handleLockCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/cooking lock <player> <recipe>");
            return;
        }

        if (args.length == 2) {
            Player player = Bukkit.getPlayer(args[0]);
            String recipe = args[1];
            if (player == null) {
                AdventureUtil.consoleMessage("<Red> [!] Player " + args[0] + " not found.");
                return;
            }
            RecipeDataUtil.setRecipeStatus(player, recipe, false);
        } else {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/cooking lock <player> <recipe>");
        }
    }

    private void handleMasteryCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/cooking mastery <player> <recipe> <count>");
            return;
        }

        if (args.length == 2) {
            Player player = Bukkit.getPlayer(args[0]);
            String recipe = args[1];
            if (player == null) {
                AdventureUtil.consoleMessage("<Red> [!] Player " + args[0] + " not found.");
                return;
            }
            RecipeDataUtil.setRecipeData(player, recipe, RecipeDataUtil.getDefaultRequiredMastery(recipe));
        }

        if (args.length == 3) {
            Player player = Bukkit.getPlayer(args[0]);
            String recipe = args[1];
            Integer count = Integer.parseInt(args[2]);
            if (player == null) {
                AdventureUtil.consoleMessage("<Red> [!] Player " + args[0] + " not found.");
                return;
            }
            RecipeDataUtil.setRecipeData(player, recipe, count);
        } else {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/cooking mastery <player> <recipe> <count>");
        }
    }

    private void handleCompetitionCommand(CommandSender sender, String[] args) {
        if (args.length < 1){
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
            return;
        }
        if (args[0].equals("start")){
            if (args.length < 2){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
                return;
            }
            if (CompetitionSchedule.startCompetition(args[1])){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceSuccess);
            } else {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceFailure);
            }
        } else if (args[0].equals("end")) {
            CompetitionSchedule.endCompetition();
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceEnd);
        } else if (args[0].equals("cancel")) {
            CompetitionSchedule.cancelCompetition();
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceCancel);
        } else if (args[0].equals("join")) {
            if (sender instanceof Player player) {
                Competition.currentCompetition.tryAddBossBarToPlayer(player);
            }
        }
    }

    private void handleRecipeBookCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            GuiManager.getRecipeBook(null).open(player);
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        GuiManager.getRecipeBook(null).open(player);
    }

    private void handleGiveItemCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/cooking give <player> <item> <amount>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }

        String itemName = args[1];
        int amount = Integer.parseInt(args[2]);

        InventoryUtil.giveItem(player, itemName, amount, true);

        AdventureUtil.sendMessage(sender, "Gave " + amount + " " + itemName + " to " + player.getName());
    }


}
