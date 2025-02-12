package plugin.customcooking.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import plugin.customcooking.functions.cooking.CookingManager;

public class InteractListener implements Listener {
    private final CookingManager cookingManager;

    public InteractListener(CookingManager cookingManager) {
        this.cookingManager = cookingManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        cookingManager.onBarInteract(event);
    }
}