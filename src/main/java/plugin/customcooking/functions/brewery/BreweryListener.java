package plugin.customcooking.functions.brewery;

import com.dre.brewery.api.events.brew.BrewModifyEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import plugin.customcooking.functions.cooking.CookingManager;
import plugin.customcooking.functions.jade.JadeManager;

import static com.dre.brewery.api.events.brew.BrewModifyEvent.Type.SEAL;
import static plugin.customcooking.functions.jade.JadeManager.give;
import static plugin.customcooking.manager.configs.ConfigManager.brewingJadeRewardRate;
import static plugin.customcooking.manager.configs.ConfigManager.brewingRequiredQuality;

public class BreweryListener implements Listener {
    public BreweryListener() {

    }

    @EventHandler
    public void onBrewEvent(BrewModifyEvent event) {
        breweryJade(event);
    }

    public void breweryJade(BrewModifyEvent event) {
            System.out.println("Ding!");
            System.out.println(event.getPlayer().getName() + " is brewing!");
        if (event.getType() != SEAL) {
            return;
        }
        Player player = event.getPlayer();
        int quality = event.getBrew().getQuality();
        if (quality >= brewingRequiredQuality && Math.random() <= brewingJadeRewardRate) {
            give(player, 1, "brewing");
        }
    }
}
