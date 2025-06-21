package plugin.borealcore.listener;

import net.momirealms.customfishing.api.event.FishingResultEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugin.borealcore.functions.jade.JadeManager;

public class FishingListener implements Listener {
    private final JadeManager jadeManager;
    public FishingListener(JadeManager jadeManager) {
        this.jadeManager = jadeManager;
    }

    @EventHandler
    public void onPlayerFishEvent(FishingResultEvent event) {
        if (event.isCancelled()) {
            return;
        }
        jadeManager.fishingJade(event);
    }
}
