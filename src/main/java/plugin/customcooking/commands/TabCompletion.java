package plugin.customcooking.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static plugin.customcooking.configs.RecipeManager.RECIPES;

public class TabCompletion implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("cook");
            completions.add("reload");
            completions.add("unlock");
            return completions;
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "cook":
                    return getOnlinePlayerNames();
                case "unlock":
                    return getAvailableRecipes();
                default:
                    break;
            }
        }

        return null; // No tab completion for other cases
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
