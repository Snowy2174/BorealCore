package plugin.borealcore.functions.traps;

import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.MechanicType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.BorealCore;
import plugin.borealcore.api.module.BorealModule;
import plugin.borealcore.api.module.ModuleContext;
import plugin.borealcore.object.DebugLevel;
import plugin.borealcore.object.Function;
import plugin.borealcore.object.SimpleListener;
import plugin.borealcore.utility.AdventureUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public class TrapsManager extends Function implements BorealModule {

    private static TrapsDatabase database;
    
    public TrapsManager() {
        database = new TrapsDatabase(BorealCore.getDatabaseManager());
        this.simpleListener = new SimpleListener(this);
    }

    public static TrapsDatabase getTrapsDatabase(){
        return database;
    }

    public static HashMap<String, Trap> TRAPS;
    private final SimpleListener simpleListener;

    @Override
    public void load() {
        AdventureUtil.consoleMessage("Loading TrapsManager...");
        TRAPS = new HashMap<>();
        // @TODO: loadItems();
        Bukkit.getPluginManager().registerEvents(new TownyListener(), BorealCore.plugin);
        Bukkit.getPluginManager().registerEvents(this.simpleListener, BorealCore.plugin);

        AdventureUtil.consoleMessage("Loaded <green>" + (TRAPS.size()) + " <gray> trap configurations");
        AdventureUtil.consoleMessage("TrapsManager loaded successfully");
    }

    @Override
    public void unload() {
        if (this.simpleListener != null) HandlerList.unregisterAll(this.simpleListener);
    }

    public boolean isBait(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        BukkitCustomFishingPlugin api = BukkitCustomFishingPlugin.getInstance();
        return !api.getEffectManager().getEffectModifier(api.getItemManager().getItemID(item), MechanicType.BAIT).isEmpty();
    }

    @Override
    public void onClickInventory(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !(inventory.getHolder(false) instanceof TrapInventory myInventory)) {
            //AdventureUtil.consoleMessage(DebugLevel.DEBUG, "Clicked inventory is not a TrapInventory but: " + inventory.getHolder());
            return;
        }
        if (inventory == null) return;
        event.setCancelled(true);
        if (!event.getClick().isLeftClick()) return;
        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (clicked != null) { //@ TODO check if the item is a fish
            if (clicked.getType() == Material.COD) {
                myInventory.takeItem((Player) event.getWhoClicked(), clicked);
                myInventory.reloadInventory();
            } else if (clicked.getType() == Material.BARRIER) {
                if (isBait(cursor)) {
                    myInventory.setBaitItem(cursor);
                    cursor.setType(Material.AIR);
                    myInventory.reloadInventory();
                } else {
                    AdventureUtil.playerMessage((Player) event.getWhoClicked(), "You can only use bait items here!");
                }
            }
        }
    }

    @Override
    public void onCloseInventory(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof TrapInventory myInventory) {
            myInventory.updateFishingTrap();
        }
    }

    @Override
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        if (event.getFurniture().getId().equals("fishing_trap")) {
            database.deleteFishingTrapById(event.getFurniture().getEntity().getUniqueId().toString());
        }
    }

    @Override
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        if (!event.getFurniture().getId().equals("fishing_trap")) {
            return;
        }
        Player player = event.getPlayer();
        Entity entity = event.getFurniture().getEntity();
        if (database.getFishingTrapById(entity.getUniqueId().toString()) != null) {
            UUID playerID = player.getUniqueId();
            Trap fishingTrap = database.getFishingTrapById(entity.getUniqueId().toString());
            if (fishingTrap.getOwner().equals(playerID)) {
                // @TODO Method to handle interacting with your own fishing trap
                consoleMessage("Opened fishing trap with id:" + fishingTrap.getUuid());
                player.openInventory(new TrapInventory(fishingTrap, BorealCore.getInstance()).getInventory());
            } else {
                // @TODO Method to handle interacting with someone else's fishing trap
            }
        } else {
            Trap fishingTrap = TrapDataManager.handleCreateFishingTrap(player, entity);
            consoleMessage(DebugLevel.DEBUG, "Created a new fishing trap! with id:" + fishingTrap.getUuid());
            player.openInventory(new TrapInventory(fishingTrap, BorealCore.getInstance()).getInventory());
        }
    }

    @Override
    public void onModuleEnable() throws Exception {

    }

    @Override
    public void onModuleDisable() throws Exception {

    }

    @Override
    public void onModuleInitialize(ModuleContext context) throws Exception {
        Connection conn = context.getDatabaseManager().getConnection("traps");
        if (conn == null) {
            return;
        }
        String createTrapsTable = "CREATE TABLE IF NOT EXISTS fishing_traps (" +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`owner` TEXT NOT NULL," +
                "`key` TEXT NOT NULL," +
                "`location` TEXT NOT NULL," +
                "`active` INTEGER NOT NULL," +
                "`items` TEXT," +
                "`maxItems` INTEGER NOT NULL," +
                "`bait` TEXT," +
                "PRIMARY KEY (`uuid`)" +
                ");";

        try (Statement s = conn.createStatement()) {
            s.executeUpdate(createTrapsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
