package plugin.customcooking.Listener;

import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugin.customcooking.Manager.FurnitureManager;

public class FurnitureListener implements Listener {

    private final FurnitureManager furnitureManager;

    public FurnitureListener(FurnitureManager furnitureManager) {
        this.furnitureManager = furnitureManager;
    }

    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        furnitureManager.onFurnitureInteract(event);
    }
}
