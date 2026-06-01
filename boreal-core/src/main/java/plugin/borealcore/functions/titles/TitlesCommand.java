package plugin.borealcore.functions.titles;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import plugin.borealcore.functions.titles.TitlesModule.TitleType;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.utility.AdventureUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TitlesCommand implements CommandExecutor, TabCompleter {

    private final TitlesModule manager;
    private final Permission titleManagerPermission;

    public TitlesCommand(TitlesModule manager) {
        this.manager = manager;
        // Moved the permission registration here since it is only used by the command
        this.titleManagerPermission = new Permission("borealcore.titlemanager", PermissionDefault.TRUE);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            AdventureUtil.consoleMessage(MessageManager.noConsole);
            return true;
        }

        if (!player.hasPermission(titleManagerPermission)) {
            AdventureUtil.playerMessage(player, MessageManager.infoNegative + "You don't have permission to manage titles.");
            return true;
        }

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("prefix")) {
                manager.openTitleScreen(player, TitleType.PREFIX);
                return true;
            } else if (args[0].equalsIgnoreCase("suffix")) {
                manager.openTitleScreen(player, TitleType.SUFFIX);
                return true;
            }
        }

        // Default to prefix view
        manager.openTitleScreen(player, TitleType.PREFIX);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player) || !player.hasPermission(titleManagerPermission)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = Arrays.asList("prefix", "suffix");
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}