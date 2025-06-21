package plugin.borealcore.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import plugin.borealcore.functions.cooking.CookingManager;

public class ConsumeItemListener implements Listener {

    private final CookingManager cookingManager;

    public ConsumeItemListener(CookingManager cookingManager) {
        this.cookingManager = cookingManager;
    }

    @EventHandler
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        cookingManager.onConsumeItem(event);
    }
}
