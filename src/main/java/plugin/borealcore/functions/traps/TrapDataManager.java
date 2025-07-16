package plugin.borealcore.functions.traps;

import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.BorealCore;
import plugin.borealcore.utility.AdventureUtil;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public class TrapDataManager {
    public static void handleFishingTrapInteract(Player player, Entity entity) {
        if (BorealCore.getTrapsDatabase().getFishingTrapById(entity.getUniqueId().toString()) != null) {
            // Method to handle interacting with a fishing trap
            UUID playerID = player.getUniqueId();
            Trap fishingTrap = BorealCore.getTrapsDatabase().getFishingTrapById(entity.getUniqueId().toString());
            if (fishingTrap.getOwner().equals(playerID)) {
                // @TODO Method to handle interacting with your own fishing trap
                consoleMessage("Opened fishing trap with id:" + fishingTrap.getUuid());
                player.openInventory(new TrapInventory(fishingTrap, BorealCore.getInstance()).getInventory());
            } else {
                // @TODO Method to handle interacting with someone else's fishing trap
            }
        } else {
            // Method to handle interacting with a fishing trap that does not have stored data
            Trap fishingTrap = handleCreateFishingTrap(player, entity);
            consoleMessage("Created a new fishing trap! with id:" + fishingTrap.getUuid());
            player.openInventory(new TrapInventory(fishingTrap, BorealCore.getInstance()).getInventory());
        }
    }

    public static Trap handleCreateFishingTrap(Player player, Entity entity) {
        // Method to handle creating a fishing trap
        Trap fishingTrap = new Trap("fishing_trap", // New fishing trap
                entity.getUniqueId(),
                player.getUniqueId(),
                entity.getLocation(),
                false,
                new ArrayList<>(),
                64, // Get from Config eventually
                null); // Get from Config eventually
        fishingTrap.checkActive(); // Check if the trap is active
        BorealCore.getTrapsDatabase().saveFishingTrap(fishingTrap); // Save the trap to the database!
        return fishingTrap;
    }

    public static void updateFishingTraps() {
        BukkitCustomFishingPlugin api = TrapsManager.getCustomFishingApi();
        Context<Player> context = Context.player(null);
        context.arg(ContextKeys.SURROUNDING, "water");
        for (Trap trap : BorealCore.getTrapsDatabase().getActiveFishingTraps()) {
            consoleMessage("Updating fishing trap with id: " + trap.getUuid());
            if (trap.getBait() == null || trap.getBait().getType() == Material.AIR) {
                continue;
            }
            context.arg(ContextKeys.LOCATION, trap.getLocation());
            context.arg(ContextKeys.OTHER_LOCATION, trap.getLocation());
            Loot loot = api.getLootManager().getNextLoot(Effect.newInstance(), context);
            if (loot.type() == LootType.ITEM) {
                ItemStack itemStack = api.getItemManager().buildInternal(context, loot.id());
                trap.addItem(itemStack);
                consoleMessage("Added item to trap: " + itemStack.getType() + " with id: " + trap.getUuid());
                BorealCore.getTrapsDatabase().saveFishingTrap(trap);
            }
        }
    }
}
