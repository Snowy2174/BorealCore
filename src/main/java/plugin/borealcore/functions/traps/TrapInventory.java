package plugin.borealcore.functions.traps;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.borealcore.BorealCore;
import plugin.borealcore.utility.AdventureUtil;

import java.util.ArrayList;
import java.util.List;

public class TrapInventory implements InventoryHolder {

    private final Inventory inventory;
    private final Trap trap;
    private static List<ItemStack> items;
    private static ItemStack infoItem;
    private static ItemStack baitItem;

    public TrapInventory(Trap trap, BorealCore plugin) {
        this.inventory = plugin.getServer().createInventory(this, 27, "Fishing Trap");
        this.trap = trap;
        items = trap.getItems();
        infoItem = getInfoItem();
        baitItem = getBaitItem();
        setInfoItems();
        // Add items to the inventory
        for (ItemStack item : items) {
            inventory.addItem(item);
        }
    }

    public ItemStack getInfoItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        modifyLore(item);
        return item;
    }


    public ItemStack getBaitItem() {
        if (trap.getBait() == null) {
            return new ItemStack(Material.BARRIER);
        }
        ItemStack item = trap.getBait();
        return item;
    }

    private void modifyLore(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
            itemStack.setItemMeta(itemMeta);
        }
        List<String> lore = itemMeta.getLore();

        if (!itemMeta.hasLore()) {
            lore = new ArrayList<>();
            lore.add("This item does not have lore! Configure it correctly in ItemsAdder!");
        }

        lore.add(" ");
        lore.add("Active: " + trap.isActive());
        lore.add("Max Items: " + trap.getMaxItems());
        lore.add("");

        List<Component> parsedLore = new ArrayList<>();

        for (String line : lore) {
            parsedLore.add(AdventureUtil.getComponentFromMiniMessage(line));
        }

        itemMeta.lore(parsedLore);
        itemStack.setItemMeta(itemMeta);
    }

    public void setInfoItems() {
        inventory.setItem(19, infoItem);
        inventory.setItem(10, baitItem);
        inventory.setItem(0, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        inventory.setItem(1, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        inventory.setItem(2, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        inventory.setItem(9, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        inventory.setItem(11, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        inventory.setItem(18, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        inventory.setItem(20, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));

        // @TODO Method to set the info items
    }

    public void takeItem(Player player, ItemStack itemStack) {
        items.remove(itemStack);
        player.getInventory().addItem(itemStack);
        // @TODO Method to take an item from the inventory
    }

    public void reloadInventory() {
        inventory.clear();
        setInfoItems();
        for (ItemStack item : items) {
            inventory.addItem(item);
        }
        // @TODO Method to reload the inventory
    }

    public void updateFishingTrap() {
        trap.setItems(items);
        BorealCore.getTrapsDatabase().saveFishingTrap(trap);
        // @TODO Method to update the fishing trap
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}