package plugin.borealcore.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import plugin.borealcore.functions.traps.TrapInventory;
import plugin.borealcore.functions.traps.TrapsManager;

public class TrapGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // We're getting the clicked inventory to avoid situations where the player
        // already has a stone in their inventory and clicks that one.
        Inventory inventory = event.getClickedInventory();
        // Add a null check in case the player clicked outside the window.
        if (inventory == null || !(inventory.getHolder(false) instanceof TrapInventory myInventory)) {
            return;
        }
        event.setCancelled(true);
        TrapsManager.handleTrapClick(event, myInventory);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof TrapInventory myInventory) {
            myInventory.updateFishingTrap();
        }
    }

}
