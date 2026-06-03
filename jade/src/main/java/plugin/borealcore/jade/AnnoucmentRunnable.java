package plugin.borealcore.jade;

import org.bukkit.entity.Player;
import plugin.borealcore.BorealCore;
import plugin.borealcore.jade.config.JadeMessage;
import plugin.borealcore.object.DebugLevel;
import plugin.borealcore.manager.MessageManager;
import plugin.borealcore.utility.AdventureUtil;

import java.util.Collection;
import java.util.List;

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
                .filter(player -> !player.hasPermission("jade.announcements"))
                .toList();
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Found " + validPlayers.size() + " valid players");
        for (Player p : validPlayers) {
            int status = JadeModule.sendJadeLimitMessage(p);
            if (status == -1) {
                AdventureUtil.playerMessage(p, MessageManager.infoPositive + JadeMessage.jadeGetStarted);
            } else if (status <= 2) {
                AdventureUtil.playerMessage(p, MessageManager.infoPositive + JadeMessage.jadeSourceReminder);
            } else {
                AdventureUtil.playerMessage(p, MessageManager.infoPositive + JadeMessage.jadeSourceReminder2);
            }
        }
    }
}
