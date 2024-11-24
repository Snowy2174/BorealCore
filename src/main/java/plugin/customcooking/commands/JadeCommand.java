package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.jade.JadeDatabase;
import plugin.customcooking.manager.JadeManager;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.util.AdventureUtil;


public class JadeCommand implements CommandExecutor {

    private final JadeManager jadeManager;
    private final JadeDatabase jadeDatabase;

    public JadeCommand() {
        this.jadeManager = CustomCooking.getJadeManager();
        this.jadeDatabase = CustomCooking.getDatabase();
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
        } else if (subcommand.equalsIgnoreCase("totalJadeForPlayer")) {
            handleTotalJadeForPlayerCommand(sender, subargs);
        } else if (subcommand.equalsIgnoreCase("totalJadeForSource")) {
            handleTotalJadeForSourceCommand(sender, subargs);
        }
        return true;
    }

    private void handleTotalJadeForPlayerCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade totalJadeForPlayer <player>");
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + MessageManager.playerNotExist);
            return;
        }

        int totalJade = jadeDatabase.getTotalJadeForPlayer(player);
        AdventureUtil.sendMessage(sender, "Total jade for " + player.getName() + ": " + totalJade);
    }

    private void handleTotalJadeForSourceCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.infoNegative + "/jade totalJadeForSource <source>");
            return;
        }

        String source = args[0];
        int totalJade = jadeDatabase.getTotalJadeFromSource(source);
        AdventureUtil.sendMessage(sender, "Total jade for source " + source + ": " + totalJade);
    }

}
