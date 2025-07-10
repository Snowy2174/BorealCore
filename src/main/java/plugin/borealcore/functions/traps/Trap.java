package plugin.borealcore.functions.traps;

import com.avaje.ebean.validation.NotNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import plugin.borealcore.action.Action;
import plugin.borealcore.functions.cooking.Difficulty;
import plugin.borealcore.functions.cooking.object.Layout;
import plugin.borealcore.functions.herbalism.objects.HerbalismType;
import plugin.borealcore.functions.herbalism.objects.Modifier;

import java.util.List;
import java.util.UUID;

public class Trap {
    public static Trap EMPTY = new Trap("null",null, null, null, false, null, 0, null);

    protected UUID id;
    protected UUID owner;
    protected String key;
    protected @NotNull Location location;
    protected boolean active;
    protected List<ItemStack> items;
    protected int maxItems;
    protected ItemStack bait;

    public Trap(String key, UUID id, UUID owner, Location location, boolean active, List<ItemStack> items, int maxItems, ItemStack bait) {
        this.key = key;
        this.id = id;
        this.owner = owner;
        this.location = location;
        this.active = active;
        this.items = items;
        this.maxItems = maxItems;
        this.bait = bait;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getKey() {
        return key;
    }

    public @NotNull Location getLocation() {
        return location;
    }

    public boolean isActive() {
        return active;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public ItemStack getBait() {
        return bait;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }

    public void setBait(ItemStack bait) {
        this.bait = bait;
    }

    public boolean isFull() {
        return items.size() >= maxItems;
    }


    public void addItem(ItemStack item) {
        items.add(item);
    }

    public boolean addBait(ItemStack item) {
        if (bait == null) {
            bait = item;
            return true;
        }
        if (bait == item) {
            bait.setAmount(bait.getAmount() + item.getAmount());
            return true;
        }
        return false;
    }

    // Method to check if the trap is active by seeing if its in water
    public void checkActive() {
        if (!active) { // AND add if bait list is not empty
            int[][] directions = {{0, 0, -1}, {0, 0, 1}, {1, 0, 0}, {-1, 0, 0}}; // This is kinda complicated, but it's just checking the blocks around the trap
            for (int[] dir : directions) {
                if (location.getBlock().getRelative(dir[0], dir[1], dir[2]).getType().equals(Material.WATER)) {
                    active = true;
                    break;
                }
            }
        }
    }

}
