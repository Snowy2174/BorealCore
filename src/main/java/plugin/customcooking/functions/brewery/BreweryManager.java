package plugin.customcooking.functions.brewery;

import com.dre.brewery.api.events.brew.BrewModifyEvent;
import org.bukkit.entity.Player;
import plugin.customcooking.functions.jade.JadeManager;

import static com.dre.brewery.api.events.brew.BrewModifyEvent.Type.FILL;
import static com.dre.brewery.api.events.brew.BrewModifyEvent.Type.SEAL;
import static plugin.customcooking.manager.configs.ConfigManager.brewingJadeRewardRate;
import static plugin.customcooking.manager.configs.ConfigManager.brewingRequiredQuality;

public class BreweryManager {
    public void breweryJade(BrewModifyEvent event) {
        if (event.getType() != SEAL) {
            return;
        }
        Player player = event.getPlayer();
        int quality = event.getBrew().getQuality();
        if (quality >= brewingRequiredQuality && Math.random() <= brewingJadeRewardRate) {
            JadeManager.give(player, 1, "brewing");
        }
    }
}