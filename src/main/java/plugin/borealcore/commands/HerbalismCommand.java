package plugin.borealcore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.cooking.CookingManager;
import plugin.borealcore.functions.herbalism.HerbalismManager;

import static plugin.borealcore.utility.AdventureUtil.playerMessage;
import static plugin.borealcore.utility.AdventureUtil.sendMessage;

public class HerbalismCommand implements CommandExecutor {

    private final HerbalismManager herbalismManager;

    public HerbalismCommand() {
        this.herbalismManager = BorealCore.getHerbalismManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("borealcore.admin")) {
            //showStatsCommand(sender);
            return true;
        }

        if (args.length == 0) {
            //showCommandHelp(sender);
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "infuse":
                if (args.length > 1) {
                    if (sender instanceof Player) {
                        herbalismManager.autoInfuse((Player) sender, Double.valueOf(args[1]), args);
                    }
                } else {
                    sendMessage(sender, "Usage: /herbalism infuse <ingredient>");
                }
                break;
            default:
                sendMessage(sender,"Unknown command. Use /herbalism help for a list of commands.");
        }
        return false;
    }
}
