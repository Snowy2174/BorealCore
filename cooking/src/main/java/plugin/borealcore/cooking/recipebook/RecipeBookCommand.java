package plugin.borealcore.cooking.recipebook;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugin.borealcore.cooking.CookingModule;
import plugin.borealcore.manager.GuiManager;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.utility.AdventureUtil;

import java.util.List;

public class RecipeBookCommand implements CommandExecutor {

    private final CookingModule cookingModule;

    // UNUSED: @todo migrate to brigadier api

    public RecipeBookCommand() {
        this.cookingModule = null; // @TODO neenaw nee naw
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

        if (subcommand.equalsIgnoreCase("plugin/borealcore/cooking")) {
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
            GuiManager.openGui(player, "cookingRecipeBook");
        }

        Player player = Bukkit.getPlayer(subargs[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        GuiManager.openGui(player, "cookingRecipeBook");
    }

    private void handleBrewingCommand(CommandSender sender, String[] subargs) {
        if (sender instanceof Player player) {
            GuiManager.openGui(player, "brewingRecipeBook");
        }

        Player player = Bukkit.getPlayer(subargs[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }
        GuiManager.openGui(player, "brewingRecipeBook");
    }

    public static class RecipeBookTabCompletion implements TabCompleter {

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            return List.of();
        }
    }
}



