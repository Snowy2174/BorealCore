package plugin.borealcore.herbalism;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

import static plugin.borealcore.utility.AdventureUtil.sendMessage;

public class HerbalismCommand implements CommandExecutor {

    private final HerbalismModule herbalismModule;

    public HerbalismCommand(HerbalismModule herbalismModule) {
        this.herbalismModule = herbalismModule;
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
                        herbalismModule.autoInfuse((Player) sender, Double.valueOf(args[1]), Arrays.copyOfRange(args, 2, args.length));
                    }
                } else {
                    sendMessage(sender, "Usage: /herbalism infuse <ingredient>");
                }
                break;
            default:
                sendMessage(sender, "Unknown command. Use /herbalism help for a list of commands.");
        }
        return false;
    }
}
