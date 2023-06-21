package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static plugin.customcooking.manager.configs.RecipeManager.RECIPES;

public class TabCompletion implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("cook");
            completions.add("reload");
            completions.add("unlock");
            completions.add("lock");
            completions.add("mastery");
            completions.add("competition");
            completions.add("recipebook");
            completions.add("migrateperms");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("competition")) {
            completions.addAll(List.of("start", "end", "cancel", "join"));
        } else if (args.length == 2) {
            completions.addAll(getOnlinePlayerNames());
        } else if (args.length == 4 && args[0].equalsIgnoreCase("cook")) {
            completions.add("auto");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("mastery")) {
            completions.add("<count>");
        } else if (args.length == 3 || args.length == 4) {
            completions.addAll(getAvailableRecipes());
        } else if (args.length == 5) {
            completions.add("<count>");
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

    private List<String> getAvailableRecipes() {
        return new ArrayList<>(RECIPES.keySet());
    }
}
