package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class JadeTabCompletion implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("customcooking.admin")) {
            completions.add("give");
            completions.add("totalJadeForPlayer");
            completions.add("totalJadeForSource");
            completions.add("getMostRecent");
            completions.add("getPlayerData");
            completions.add("verifyAndFixTotals");
        } else if (args.length == 1) {
            completions.add("top");
            completions.add("balance");
            completions.add("limits");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("totalJadeForSource")) {
            completions.addAll(List.of("cooking", "farming", "fishing", "spirit", "mastery"));
        } else if (args.length == 2) {
            completions.addAll(getOnlinePlayerNames());
        }
        return completions;
    }

    private List<String> getOnlinePlayerNames() {
        List<String> completions = new ArrayList<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            completions.add(player.getName());
        }
        return completions;
    }
}
