package plugin.borealcore.api.module;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base abstract class for all modular BorealCore GUIs.
 * Implements InventoryHolder to natively hook into Bukkit's inventory system.
 */
public abstract class BorealGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> clickActions;
    private final int size;

    /**
     * Creates a new GUI with a legacy string title.
     */
    public BorealGUI(int rows, String title) {
        this.size = rows * 9;
        this.clickActions = new HashMap<>();
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    /**
     * Creates a new GUI with a Paper Adventure Component title.
     */
    public BorealGUI(int rows, Component title) {
        this.size = rows * 9;
        this.clickActions = new HashMap<>();
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    /**
     * Initialize the GUI contents for the specific player viewing it.
     * Replaces the `init(Player, InventoryContents)` from SmartInventory.
     * * @param player The player opening the GUI.
     */
    public abstract void init(Player player);

    /**
     * Optional update method if you want to implement repeating refresh tasks later.
     */
    public void update(Player player) {
        // Override in specific GUIs if needed
    }

    // --- Item Setting Methods ---

    /**
     * Sets an item in a specific raw slot with an optional click action.
     */
    public void setItem(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
        if (slot >= 0 && slot < size) {
            inventory.setItem(slot, item);
            if (action != null) {
                clickActions.put(slot, action);
            } else {
                clickActions.remove(slot);
            }
        }
    }

    /**
     * Sets an item without a specific action (just a display item).
     */
    public void setItem(int slot, ItemStack item) {
        setItem(slot, item, null);
    }

    /**
     * Sets an item using Row and Column coordinates (0-indexed).
     * e.g., row 5, col 4 = slot 49.
     */
    public void setItem(int row, int col, ItemStack item, Consumer<InventoryClickEvent> action) {
        setItem(row * 9 + col, item, action);
    }

    // --- Fill Methods ---

    /**
     * Fills all empty slots in the inventory with the specified item and action.
     */
    public void fill(ItemStack item, Consumer<InventoryClickEvent> action) {
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i) == null) {
                setItem(i, item, action);
            }
        }
    }

    /**
     * Fills the outermost border of the inventory with the specified item and action.
     */
    public void fillBorders(ItemStack item, Consumer<InventoryClickEvent> action) {
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == (size / 9) - 1 || col == 0 || col == 8) {
                setItem(i, item, action);
            }
        }
    }

    // --- Core Logic ---

    /**
     * Opens this inventory for the specified player.
     * Calls init() automatically before opening.
     */
    public void open(Player player) {
        init(player);
        player.openInventory(this.inventory);
    }

    /**
     * Handles the click event by looking up the registered consumer for the slot.
     */
    public void handleClick(InventoryClickEvent event) {
        // By default, cancel clicks in custom GUIs so players can't steal items
        event.setCancelled(true);

        // Execute the attached action if one exists
        Consumer<InventoryClickEvent> action = clickActions.get(event.getRawSlot());
        if (action != null) {
            action.accept(event);
        }
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}