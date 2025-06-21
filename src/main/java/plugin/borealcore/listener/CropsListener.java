package plugin.borealcore.listener;

import net.momirealms.customcrops.api.core.block.BreakReason;
import net.momirealms.customcrops.api.event.CropBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugin.borealcore.functions.jade.JadeManager;

public class CropsListener implements Listener {
    private final JadeManager jadeManager;
    public CropsListener(JadeManager jadeManager) {
        this.jadeManager = jadeManager;
    }

    @EventHandler
    public void onCropBreakEvent(CropBreakEvent event) {
        if (event.isCancelled() || event.reason() != BreakReason.ACTION ) {
            return;
        }
        System.out.println("Processing farmingJade for player: " + event.entityBreaker());
        jadeManager.farmingJade(event);
    }
}
