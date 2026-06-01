package plugin.borealcore.object;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import plugin.borealcore.api.module.BorealGUI;

public class GUIListener {

    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof BorealGUI) {
            event.setCancelled(true);
            BorealGUI gui = (BorealGUI) event.getInventory().getHolder();
            gui.handleClick(event);
        }
    }

}
