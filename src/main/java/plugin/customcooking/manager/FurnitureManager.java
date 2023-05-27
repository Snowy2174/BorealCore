package plugin.customcooking.manager;

import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import plugin.customcooking.CustomCooking;
import plugin.customcooking.listener.FurnitureListener;
import plugin.customcooking.minigame.Function;
import plugin.customcooking.util.AdventureUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FurnitureManager extends Function {

    private final FurnitureListener furnitureListener;
    private final Map<Player, Long> cooldowns;

    public FurnitureManager() {
        this.furnitureListener = new FurnitureListener(this);
        this.cooldowns = new HashMap<>();
        load();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.furnitureListener, CustomCooking.plugin);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this.furnitureListener);
    }

    public void onFurnitureInteract(FurnitureInteractEvent event) {
        Player player = event.getPlayer();
        CustomFurniture clickedFurniture = event.getFurniture();

        // Check if the clicked block is an unlit cookingpot
        if (clickedFurniture.getId().equals("cooking_pot_unlit")) {
            if (!cooldowns.containsKey(player) || (System.currentTimeMillis() - cooldowns.get(player) >= 2000)) {
                // Set a cooldown of 2 seconds
                cooldowns.put(player, System.currentTimeMillis());
            // Check if the player right-clicked with flint and steel
            if (player.getInventory().getItemInMainHand().getType() == Material.FLINT_AND_STEEL) {

                    ItemFrame unlitpot = (ItemFrame) Objects.requireNonNull(clickedFurniture).getArmorstand();
                    Rotation rot = unlitpot.getRotation();
                    ItemFrame litpot = (ItemFrame) CustomFurniture.spawnPreciseNonSolid("cooking_pot_lit", unlitpot.getLocation()).getArmorstand();
                    litpot.setRotation(rot);

                    // Replace the furniture block with the lit furniture
                    clickedFurniture.remove(false);
                    unlitpot.getLocation().getBlock().setType(Material.BARRIER);

                    AdventureUtil.playerMessage(player, "<grey>[<green><bold>!</bold><grey>] <green>You replaced the furniture with the lit version!");
                } else {
                AdventureUtil.playerMessage(player, "<grey>[<red><bold>!</bold><grey>] <red>You can't cook in an cold pot.. try heating it up");
            }
            } else {
                AdventureUtil.playerMessage(player, "<grey>[<red><bold>!</bold><grey>] <red>You need to wait " + ((2000 - (System.currentTimeMillis() - cooldowns.get(player))) / 1000) + " seconds before interacting with that again.");
            }
        }
    }
}
