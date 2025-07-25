package plugin.borealcore.listener;

import dev.lone.itemsadder.api.CustomStack;
import net.momirealms.customcrops.api.event.CropInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import plugin.borealcore.functions.template.PotInventory;
import plugin.borealcore.functions.traps.TrapInventory;
import plugin.borealcore.functions.traps.TrapsManager;
import plugin.borealcore.manager.configs.DebugLevel;

import java.util.EventListener;
import java.util.Objects;

import static plugin.borealcore.manager.configs.DebugLevel.DEBUG;
import static plugin.borealcore.utility.AdventureUtil.consoleMessage;
import static plugin.borealcore.utility.InventoryUtil.buildia;

public class CropInteractEventListener implements Listener {

    @EventHandler
    public void onCropInteract(CropInteractEvent event) {
        if (event.itemInHand() != null) {
            CustomStack customStack = CustomStack.byItemStack(event.itemInHand());
            consoleMessage(DebugLevel.DEBUG, "CustomStack retrieved: " + (customStack != null ? customStack.getId() : "null"));
            if (customStack != null && Objects.equals(customStack.getId(), "soil_detector")) {
                consoleMessage(DebugLevel.DEBUG,"Detected soil_detector in hand. Creating PotInventory...");
                PotInventory myInventory = new PotInventory(event.blockState(), event.location(), event.cropConfig());
                consoleMessage(DebugLevel.DEBUG, "Opening inventory for player: " + event.getPlayer().getName());
                event.getPlayer().openInventory(myInventory.getInventory());
            }

        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !(inventory.getHolder(false) instanceof PotInventory myInventory)) {
            return;
        }
        event.setCancelled(true);
        myInventory.handleClick(event);
    }
}
