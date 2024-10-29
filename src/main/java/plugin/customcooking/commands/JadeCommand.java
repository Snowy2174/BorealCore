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
import plugin.customcooking.manager.JadeManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.util.AdventureUtil;
import plugin.customcooking.util.ConfigUtil;
import plugin.customcooking.util.InventoryUtil;
import plugin.customcooking.util.RecipeDataUtil;

import java.util.List;

import static plugin.customcooking.manager.DataManager.getRecipeCount;


public class JadeCommand implements CommandExecutor {

    private final JadeManager jadeManager;

    public JadeCommand() {
        this.jadeManager = CustomCooking.getJadeManager();
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
            jadeManager.handleGiveJadeCommand(sender, subargs);
        }
        return true;
    }

}
