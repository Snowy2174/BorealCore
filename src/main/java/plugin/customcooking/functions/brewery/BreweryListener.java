package plugin.customcooking.functions.brewery;

import com.dre.brewery.api.events.brew.BrewModifyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import plugin.customcooking.functions.cooking.CookingManager;

public class BreweryListener {
        private final BreweryManager breweryManager;

        public BreweryListener(BreweryManager breweryManager) {
            this.breweryManager = breweryManager;
        }

        @EventHandler
        public void onBrewEvent(BrewModifyEvent event) {
            breweryManager.breweryJade(event);
        }
}
