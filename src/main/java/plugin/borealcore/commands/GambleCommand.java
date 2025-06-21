package plugin.borealcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.functions.plushies.PlushieManager;


public class GambleCommand implements CommandExecutor {

    private final PlushieManager plushieManager;

    public GambleCommand() {
        this.plushieManager = BorealCore.getPlushieManager();
    }

@Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("gamble.use")) {
        sender.sendMessage("You do not have permission to use this command.");
        return true;
    }

    Player player;
    int amount = 1;

    if (args.length >= 1) {
        player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage("Target player not found.");
            return true;
        }
    } else {
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender.sendMessage("You must specify a player when using this command from console.");
            return true;
        }
    }

    if (args.length >= 2) {
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount. Please provide a number.");
            return true;
        }
    }

    if (amount < 1) {
        sender.sendMessage("§cInvalid amount.");
        return true;
    }

    for (int i = 0; i < amount; i++) {
        plushieManager.processGamble(sender instanceof Player ? (Player) sender : null, player, 1);
    }

    return true;
}
}
