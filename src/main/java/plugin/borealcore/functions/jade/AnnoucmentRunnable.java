package plugin.borealcore.functions.jade;

import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.configs.MessageManager;
import plugin.borealcore.utility.AdventureUtil;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class AnnoucmentRunnable implements Runnable {

    private final BorealCore plugin;

    public AnnoucmentRunnable(BorealCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        if (players.isEmpty()) {
            return;
        }
        List<? extends Player> validPlayers = players.stream()
                .filter(player -> !player.hasPermission("jade.announcement"))
                .toList();
        plugin.getLogger().log(Level.INFO, "Found " + validPlayers.size() + " valid players");
        for (Player p : validPlayers) {
            int status = JadeManager.sendJadeLimitMessage(p);
            if (status == -1) {
                AdventureUtil.playerMessage(p, MessageManager.infoPositive + MessageManager.jadeGetStarted);
            } else if (status <= 2) {
                AdventureUtil.playerMessage(p, MessageManager.infoPositive + MessageManager.jadeSourceReminder);
            } else {
                AdventureUtil.playerMessage(p, MessageManager.infoPositive + MessageManager.jadeSourceReminder2);
            }
        }
    }
}
