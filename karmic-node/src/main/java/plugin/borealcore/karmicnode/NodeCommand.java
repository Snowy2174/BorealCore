package plugin.borealcore.karmicnode;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class NodeCommand implements CommandExecutor {

    public NodeCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("borealcore.admin")) {
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        String subcommand = args[0];
        String[] subargs = new String[args.length - 1];
        System.arraycopy(args, 1, subargs, 0, subargs.length);

        if (subcommand.equalsIgnoreCase("updateMaxWave")) {
            NodeModule.handleUpdateMaxWave(subargs[0], Integer.parseInt(subargs[1]));
        } else if (subcommand.equalsIgnoreCase("getMaxWave")) {
            sender.sendMessage(String.valueOf(NodeModule.getMaxWave(subargs[0])));
        } else if (subcommand.equalsIgnoreCase("getLeaderboardEntry")) {
            NodeModule.getLeaderboardEntry(Integer.parseInt(subargs[0]));
        } else {
            return false;

        }
        return true;
    }
}
