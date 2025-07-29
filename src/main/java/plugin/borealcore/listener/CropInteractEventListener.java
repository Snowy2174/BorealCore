package plugin.borealcore.listener;

import dev.lone.itemsadder.api.CustomStack;
import net.momirealms.customcrops.api.core.block.PotBlock;
import net.momirealms.customcrops.api.core.mechanic.fertilizer.Fertilizer;
import net.momirealms.customcrops.api.core.mechanic.fertilizer.FertilizerConfig;
import net.momirealms.customcrops.api.event.CropInteractEvent;
import net.momirealms.customcrops.api.event.FertilizerUseEvent;
import net.momirealms.customcrops.api.event.PotInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import plugin.borealcore.functions.template.PotInventory;
import plugin.borealcore.manager.configs.DebugLevel;

import java.util.Arrays;
import java.util.Objects;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public class CropInteractEventListener implements Listener {

    @EventHandler
    public void onCropInteract(CropInteractEvent event) {
        if (event.itemInHand() != null) {
            CustomStack customStack = CustomStack.byItemStack(event.itemInHand());
            consoleMessage(DebugLevel.DEBUG, "CustomStack retrieved: " + (customStack != null ? customStack.getId() : "null"));
            if (customStack != null && Objects.equals(customStack.getId(), "soil_detector")) {
                consoleMessage(DebugLevel.DEBUG, "Detected soil_detector in hand. Creating PotInventory...");
                PotInventory myInventory = new PotInventory(event.blockState(), event.location(), event.cropConfig());
                consoleMessage(DebugLevel.DEBUG, "Opening inventory for player: " + event.getPlayer().getName());
                event.getPlayer().openInventory(myInventory.getInventory());
            }

        }
    }

    @EventHandler
    public void onFertilizerUse(FertilizerUseEvent event) {
        PotBlock potBlock = (PotBlock) event.blockState().type();
        Arrays.stream(potBlock.fertilizers(event.blockState())).findFirst()
                .ifPresent(fertilizer -> {
                    if (fertilizer.config() == event.fertilizer().config()) {
                        if (event.fertilizer().times() == fertilizer.times()) {
                            consoleMessage(DebugLevel.DEBUG, "Fertilizer times match: " + fertilizer.times());
                            event.setCancelled(true);
                        }
                    }
                });
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
