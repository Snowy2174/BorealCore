package plugin.borealcore.functions.traps;

import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.BorealCore;
import plugin.borealcore.object.Function;
import plugin.borealcore.object.SimpleListener;
import plugin.borealcore.utility.AdventureUtil;

import java.util.HashMap;

public class TrapsManager extends Function {

    public TrapsManager() {
        this.simpleListener = new SimpleListener(this);
    }

    public static HashMap<String, Trap> TRAPS;
    private static BukkitCustomFishingPlugin customFishingApi;
    private final SimpleListener simpleListener;

    @Override
    public void load() {
        AdventureUtil.consoleMessage("Loading TrapsManager...");
        TRAPS = new HashMap<>();
        // @TODO: loadItems();
        Bukkit.getPluginManager().registerEvents(new TownyListener(), BorealCore.plugin);

        Bukkit.getPluginManager().registerEvents(this.simpleListener, BorealCore.plugin);
        if (DecentHologramsAPI.isRunning()) {
            // Custom Fishing API Loaded
            customFishingApi = BukkitCustomFishingPlugin.getInstance();
        }
        AdventureUtil.consoleMessage("Loaded <green>" + (TRAPS.size()) + " <gray> trap configurations");
        AdventureUtil.consoleMessage("TrapsManager loaded successfully");
    }

    @Override
    public void unload() {
        if (this.simpleListener != null) HandlerList.unregisterAll(this.simpleListener);
        // clear map
    }

    public static BukkitCustomFishingPlugin getCustomFishingApi() {
        return customFishingApi;
    }

    @Override
    public void onClickInventory(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !(inventory.getHolder(false) instanceof TrapInventory myInventory)) {
            return;
        }
        event.setCancelled(true);
        if (!event.getClick().isLeftClick()) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() == Material.COD) { //@ TODO check if the item is a fish
            myInventory.takeItem((Player) event.getWhoClicked(), clicked);
            myInventory.reloadInventory();
        }
    }

    @Override
    public void onCloseInventory(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof TrapInventory myInventory) {
            myInventory.updateFishingTrap();
        }
    }
}
