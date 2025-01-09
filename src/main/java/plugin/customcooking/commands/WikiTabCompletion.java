package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import plugin.customcooking.wiki.WikiManager;

import java.util.ArrayList;
import java.util.List;

import static plugin.customcooking.manager.configs.RecipeManager.RECIPES;

public class WikiTabCompletion implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("open");
            completions.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("open")) {
         completions.addAll(getWikiPages());
        } else if (args.length == 3) {
            completions.addAll(getOnlinePlayerNames());
        } else if (args.length == 4) {
            completions.add("<page>");
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

    private List<String> getWikiPages() {
        return new ArrayList<>(WikiManager.WIKI.keySet());
    }
}
