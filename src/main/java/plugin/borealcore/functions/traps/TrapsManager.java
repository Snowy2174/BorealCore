package plugin.borealcore.functions.traps;

import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.BorealCore;
import plugin.borealcore.listener.TownyListener;
import plugin.borealcore.listener.TrapGUIListener;
import plugin.borealcore.object.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.util.HashMap;

public class TrapsManager extends Function {

    public TrapsManager() {}

    public static HashMap<String, Trap> TRAPS;
    private static BukkitCustomFishingPlugin customFishingApi;

    @Override
    public void load() {
        TRAPS = new HashMap<>();
        // @TODO: loadItems();
        Bukkit.getPluginManager().registerEvents(new TrapGUIListener(), BorealCore.plugin);
        Bukkit.getPluginManager().registerEvents(new TownyListener(), BorealCore.plugin);

        if (DecentHologramsAPI.isRunning()) {
            // Custom Fishing API Loaded
            this.customFishingApi = BukkitCustomFishingPlugin.getInstance();
        }
        AdventureUtil.consoleMessage("Loaded <green>" + (TRAPS.size()) + " <gray> trap configurations");
        AdventureUtil.consoleMessage("TrapsManager loaded successfully");
    }

    @Override
    public void unload() {
        // clear map
    }

    public static BukkitCustomFishingPlugin getCustomFishingApi() {
        return customFishingApi;
    }

    public static void handleTrapClick(InventoryClickEvent event, TrapInventory myInventory) {
        if (!event.getClick().isLeftClick()) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() == Material.COD) { //@ TODO check if the item is a fish
            myInventory.takeItem((Player) event.getWhoClicked(), clicked);
            myInventory.reloadInventory();
        }
    }

}
