package plugin.borealcore.manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.api.module.BorealGUI;
import plugin.borealcore.utility.DebugLevel;
import plugin.borealcore.api.Function;
import plugin.borealcore.utility.AdventureUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GuiManager extends Function {

    private static final Map<String, Supplier<BorealGUI>> guiRegistry = new HashMap<>();

    @Override
    public void load() {
    }

    @Override
    public void unload() {
        guiRegistry.clear();
    }

    /**
     * Registers a new GUI factory to the core manager.
     * External modules should call this during their onModuleEnable() phase.
     *
     * @param id          The unique identifier for this GUI (e.g., "wikiMenu")
     * @param guiSupplier A supplier that creates a new instance of the BorealGUI
     */
    public void registerGui(String id, Supplier<BorealGUI> guiSupplier) {
        guiRegistry.put(id, guiSupplier);
        AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Registered GUI: " + id);
    }

    /**
     * Unregisters a GUI from the manager.
     * External modules should call this during their onModuleDisable() phase.
     */
    public void unregisterGui(String id) {
        guiRegistry.remove(id);
    }

    /**
     * Opens a registered GUI for a player.
     *
     * @param player The player to open the GUI for
     * @param id     The unique identifier of the GUI
     * @return true if the GUI was found and opened, false otherwise
     */
    public static boolean openGui(Player player, String id) {
        Supplier<BorealGUI> supplier = guiRegistry.get(id);
        if (supplier != null) {
            BorealGUI gui = supplier.get();
            gui.open(player); // Native open method from the abstract BorealGUI class
            return true;
        } else {
            AdventureUtil.consoleMessage(DebugLevel.WARNING, "Attempted to open unregistered GUI: " + id);
            return false;
        }
    }
}