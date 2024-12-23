package plugin.customcooking.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.karmicnode.NodeManager;


public class NodeCommand implements CommandExecutor {

    private final NodeManager nodeManager;

    public NodeCommand() {
        this.nodeManager = CustomCooking.getNodeManager();
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

        if (subcommand.equalsIgnoreCase("updateMaxWave")) {
            nodeManager.handleUpdateMaxWave(subargs[0], Integer.parseInt(subargs[1]));
        } else if (subcommand.equalsIgnoreCase("getMaxWave")) {
            sender.sendMessage(String.valueOf(nodeManager.getMaxWave(subargs[0])));
        } else if (subcommand.equalsIgnoreCase("getLeaderboardEntry")) {
            nodeManager.getLeaderboardEntry(Integer.parseInt(subargs[0]));
        }
        else {
            return false;

        }
        return true;
    }
}
