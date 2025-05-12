package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import plugin.customcooking.functions.jade.LeaderboardType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static plugin.customcooking.functions.jade.JadeManager.jadeSources;

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
            completions.add("leaderboard");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("totalJadeForSource")) {
            completions.addAll(jadeSources.keySet());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("leaderboard")) {
            completions.addAll(Arrays.stream(LeaderboardType.values()).map(Enum::name).collect(Collectors.toList()));completions.addAll(Arrays.stream(LeaderboardType.values()).map(Enum::name).collect(Collectors.toList()));completions.addAll(Arrays.stream(LeaderboardType.values()).map(Enum::name).collect(Collectors.toList()));
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
