package plugin.customcooking.functions.jade;

import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.utility.AdventureUtil;

import java.util.Collection;
import java.util.List;

public class AnnoucmentRunnable implements Runnable {

    private final CustomCooking plugin;

    public AnnoucmentRunnable(CustomCooking plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();
        if (players.isEmpty()) {
            return;
        }
        List<? extends Player> validPlayers = players.stream()
                .filter(player -> !player.hasPermission("customcooking.jade.announcement"))
                .toList();
        System.out.println("Jade announcement runnable executed for " + validPlayers.size() + " players.");
        for (Player p : validPlayers) {
            int status = JadeManager.sendJadeLimitMessage(p);
            if (status == -1) {
                AdventureUtil.playerMessage(p,MessageManager.infoPositive + MessageManager.jadeGetStarted);
            } else if (status <= 2) {
                AdventureUtil.playerMessage(p,MessageManager.infoPositive + MessageManager.jadeSourceReminder);
            } else {
                AdventureUtil.playerMessage(p,MessageManager.infoPositive + MessageManager.jadeSourceReminder2);
            }
        }
    }
}
