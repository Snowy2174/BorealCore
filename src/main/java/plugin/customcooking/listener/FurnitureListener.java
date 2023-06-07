package plugin.customcooking.listener;

import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugin.customcooking.manager.FurnitureManager;

public class FurnitureListener implements Listener {

    private final FurnitureManager furnitureManager;

    public FurnitureListener(FurnitureManager furnitureManager) {
        this.furnitureManager = furnitureManager;
    }

    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        furnitureManager.onFurnitureInteract(event);
    }

    @EventHandler
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        furnitureManager.onFurnitureBreak(event);
    }
}
