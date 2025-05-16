package plugin.customcooking.listener;

import com.dre.brewery.api.events.brew.BrewModifyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugin.customcooking.functions.jade.JadeManager;

import static com.dre.brewery.api.events.brew.BrewModifyEvent.Type.SEAL;

public class BreweryListener implements Listener {
    private final JadeManager jadeManager;

    public BreweryListener(JadeManager jadeManager) {
        this.jadeManager = jadeManager;
    }

    @EventHandler
    public void onBrewEvent(BrewModifyEvent event) {
        if (event.getType() != SEAL || event.isCancelled() || event.getPlayer() == null) {
            return;
        }
        jadeManager.breweryJade(event);
    }

}
