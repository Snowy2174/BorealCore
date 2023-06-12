package plugin.customcooking.util;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import plugin.customcooking.CustomCooking;

import java.util.HashMap;
import java.util.Map;

import static plugin.customcooking.util.InventoryUtil.buildia;

public class HologramUtil {

    private static final Map<Location, Hologram> holograms = new HashMap<>();

    public static void createHologram(ItemStack recipe, Location location, Boolean success) {

        if (holograms.containsKey(location)) {
            // If a hologram at that location already exists, stop
        } else {
            HolographicDisplaysAPI api = HolographicDisplaysAPI.get(CustomCooking.plugin);
            Hologram hologram = api.createHologram(location);

            if (success) {
                hologram.getLines().appendText(ChatColor.GREEN + "Success!");
            } else {
                hologram.getLines().appendText(ChatColor.RED + "Failure!");
            }

            hologram.getLines().appendText(recipe.getItemMeta().getDisplayName());
            hologram.getLines().appendItem(recipe);

            holograms.put(location, hologram);

            // Schedule a task to remove the hologram after a set time
            new BukkitRunnable() {
                @Override
                public void run() {
                    holograms.remove(location);
                    hologram.delete(); // Remove the hologram
                }
            }.runTaskLater(CustomCooking.plugin, 40);
        }
    }
}
