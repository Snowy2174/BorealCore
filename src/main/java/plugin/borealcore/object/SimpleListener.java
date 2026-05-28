package plugin.borealcore.object;

import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public record SimpleListener(Function function) implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        function.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        function.onQuit(event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        function.onInteract(event);
    }

    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        function.onFurnitureInteract(event);
    }

    @EventHandler
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        function.onFurnitureBreak(event);
    }

    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        function.onClickInventory(event);
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        function.onCloseInventory(event);
    }

    @EventHandler
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        function.onConsumeItem(event);
    }
}