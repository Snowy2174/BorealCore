package plugin.borealcore.object;

//import com.comphenix.protocol.events.PacketContainer;

import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public abstract class Function {

    public void load() {
        //empty
    }

    public void unload() {
        //empty
    }

    public void onQuit(Player player) {
        //empty
    }

    public void onJoin(Player player) {
        //empty
    }

    public void onInteract(PlayerInteractEvent event) {
        //empty
    }

    public void onCloseInventory(InventoryCloseEvent event) {
        //empty
    }

    public void onClickInventory(InventoryClickEvent event) {
        //empty
    }

    public void onOpenInventory(InventoryOpenEvent event) {
        //empty
    }

    public void onConsumeItem(PlayerItemConsumeEvent event) {
        //empty
    }

    public void onFurnitureInteract(FurnitureInteractEvent event) {
        //empty
    }

    public void onFurnitureBreak(FurnitureBreakEvent event) {
        //empty
    }
}