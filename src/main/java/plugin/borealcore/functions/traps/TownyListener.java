package plugin.borealcore.functions.traps;

import com.palmergames.bukkit.towny.event.NewDayEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownyListener implements Listener {

    @EventHandler
    public void onNewDay(NewDayEvent event) {
        TrapDataManager.updateFishingTraps();
        // @TODO add logic for new day event
    }
}
