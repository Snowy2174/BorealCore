package plugin.borealcore.functions.traps;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import plugin.borealcore.BorealCore;
import plugin.borealcore.manager.configs.DebugLevel;

import java.util.ArrayList;

import static plugin.borealcore.utility.AdventureUtil.consoleMessage;

public class TrapDataManager {

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
        fishingTrap.checkActive();
        BorealCore.getTrapsDatabase().saveFishingTrap(fishingTrap);
        return fishingTrap;
    }

    public static void updateFishingTraps() {
        BukkitCustomFishingPlugin api = BukkitCustomFishingPlugin.getInstance();
        Context<Player> context = Context.player(null);
        context.arg(ContextKeys.SURROUNDING, "water");
        for (Trap trap : BorealCore.getTrapsDatabase().getActiveFishingTraps()) {
            consoleMessage("Updating fishing trap with id: " + trap.getUuid());
            if (trap.getBait() == null || trap.getBait().getType() == Material.AIR) {
                //continue;
                consoleMessage(DebugLevel.DEBUG, "No bait found for trap with id: " + trap.getUuid() + ", skipping loot generation.");
            }
            context.arg(ContextKeys.LOCATION, trap.getLocation());
            context.arg(ContextKeys.OTHER_LOCATION, trap.getLocation());
            Loot loot = api.getLootManager().getNextLoot(Effect.newInstance(), context);
            consoleMessage(loot.id());
            if (loot.type() == LootType.ITEM) {
                ItemStack itemStack = api.getItemManager().buildInternal(context, loot.id());
                trap.addItem(itemStack);
                consoleMessage(DebugLevel.DEBUG, "Added item to trap: " + itemStack.getType() + " with id: " + trap.getUuid());
                BorealCore.getTrapsDatabase().saveFishingTrap(trap);
            }
        }
    }
}
