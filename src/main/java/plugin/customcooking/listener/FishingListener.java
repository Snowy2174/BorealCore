package plugin.customcooking.listener;

import net.momirealms.customcrops.api.event.CropBreakEvent;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugin.customcooking.functions.jade.JadeManager;

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
