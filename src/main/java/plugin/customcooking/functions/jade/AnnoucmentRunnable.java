package plugin.customcooking.functions.jade;

import org.bukkit.entity.Player;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.manager.configs.MessageManager;
import plugin.customcooking.utility.AdventureUtil;

public class AnnoucmentRunnable implements Runnable {

    private final CustomCooking plugin;

    public AnnoucmentRunnable(CustomCooking plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
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
